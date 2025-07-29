package muse2import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticalAnalyzer[object Object]
    public static class AnalysisResult {
        public double meanAccuracy;
        public double standardDeviation;
        public double standardError;
        public double confidenceInterval95     public double threshold75     public double threshold50        public Map<String, Double> conditionMeans;
        public List<String> significantEffects;
        public String summary;
        
        public String getDetailedReport() {
            StringBuilder report = new StringBuilder();
            report.append("=== Statistical Analysis Report ===\n");
            report.append(String.format(Mean Accuracy: %0.2f%% ± %.2%% (95%% CI)\n", 
                meanAccuracy * 100, confidenceInterval95 * 100            report.append(String.format("Standard Deviation: %.3 standardDeviation));
            report.append(String.format("Standard Error: %.3f\n", standardError));
            report.append(String.format("75%% Threshold: %.1f μs\n, threshold75));
            report.append(String.format("50%% Threshold: %.1f μs\n, threshold50      
            if (!conditionMeans.isEmpty())[object Object]            report.append(undefinednCondition Means:\n");
                for (Map.Entry<String, Double> entry : conditionMeans.entrySet()) {
                    report.append(String.format("  %s: %.2%\n, entry.getKey(), entry.getValue() * 100));
                }
            }
            
            if (!significantEffects.isEmpty())[object Object]            report.append("\nSignificant Effects:\n");
                for (String effect : significantEffects) {
                    report.append(+ effect + "\n");
                }
            }
            
            return report.toString();
        }
    }
    
    // Analyze participant responses from CSV file
    public static AnalysisResult analyzeResponses(String csvFile) throws IOException {
        List<ResponseData> responses = loadResponses(csvFile);
        return analyzeResponseData(responses);
    }
    
    // Analyze multiple participant files
    public static AnalysisResult analyzeMultipleParticipants(List<String> csvFiles) throws IOException {
        List<ResponseData> allResponses = new ArrayList<>();
        for (String file : csvFiles)[object Object]        allResponses.addAll(loadResponses(file));
        }
        return analyzeResponseData(allResponses);
    }
    
    // Load response data from CSV
    private static List<ResponseData> loadResponses(String csvFile) throws IOException {
        List<ResponseData> responses = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null)[object Object]            String[] parts = line.split(",");
                if (parts.length >= 10) {
                    ResponseData data = new ResponseData();
                    data.participantId = parts[0];
                    data.trialNumber = Integer.parseInt(parts[2]);
                    data.stimulus = parts[3];
                    data.itd = Double.parseDouble(parts[4]);
                    data.ild = Double.parseDouble(parts[5]);
                    data.frequencyShift = Double.parseDouble(parts[6]);
                    data.response = parts[7];
                    data.responseTime = Long.parseLong(parts[8]);
                    data.correct = parts[9].equals("1");
                    data.condition = parts[10];
                    responses.add(data);
                }
            }
        }
        
        return responses;
    }
    
    // Perform statistical analysis
    private static AnalysisResult analyzeResponseData(List<ResponseData> responses) [object Object]    AnalysisResult result = new AnalysisResult();
        
        // Calculate basic statistics
        List<Double> accuracies = responses.stream()
            .mapToDouble(r -> r.correct ? 1.0 : 0)
            .boxed()
            .collect(Collectors.toList());
        
        result.meanAccuracy = calculateMean(accuracies);
        result.standardDeviation = calculateStandardDeviation(accuracies);
        result.standardError = result.standardDeviation / Math.sqrt(accuracies.size());
        result.confidenceInterval95 * result.standardError; // 95% CI
        
        // Calculate thresholds using psychometric function
        result.threshold75 = calculateThreshold(responses, 0.75
        result.threshold50 = calculateThreshold(responses, 0.50);
        
        // Analyze by condition
        result.conditionMeans = calculateConditionMeans(responses);
        
        // Perform significance tests
        result.significantEffects = performSignificanceTests(responses);
        
        // Generate summary
        result.summary = generateSummary(result);
        
        return result;
    }
    
    // Calculate psychometric threshold
    private static double calculateThreshold(List<ResponseData> responses, double targetAccuracy) [object Object]       // Group by ITD magnitude and calculate accuracy for each
        Map<Double, List<ResponseData>> groupedByITD = responses.stream()
            .collect(Collectors.groupingBy(r -> Math.abs(r.itd)));
        
        List<Double> itdValues = new ArrayList<>(groupedByITD.keySet());
        Collections.sort(itdValues);
        
        // Find ITD value closest to target accuracy
        double closestITD = 00        double minDifference = Double.MAX_VALUE;
        
        for (Double itd : itdValues) [object Object]
            List<ResponseData> group = groupedByITD.get(itd);
            double accuracy = group.stream()
                .mapToDouble(r -> r.correct ? 1.0 : 0.0
                .average()
                .orElse(00      
            double difference = Math.abs(accuracy - targetAccuracy);
            if (difference < minDifference)[object Object]               minDifference = difference;
                closestITD = itd;
            }
        }
        
        return closestITD;
    }
    
    // Calculate means by condition
    private static Map<String, Double> calculateConditionMeans(List<ResponseData> responses) {
        return responses.stream()
            .collect(Collectors.groupingBy(
                r -> r.condition,
                Collectors.averagingDouble(r -> r.correct ? 1.0 : 0)
            ));
    }
    
    // Perform significance tests
    private static List<String> performSignificanceTests(List<ResponseData> responses) {
        List<String> significantEffects = new ArrayList<>();
        
        // Test for condition effects
        Map<String, List<ResponseData>> byCondition = responses.stream()
            .collect(Collectors.groupingBy(r -> r.condition));
        
        if (byCondition.size() > 1) {
            // Perform ANOVA-like test (simplified)
            double fStatistic = calculateFStatistic(byCondition);
            if (fStatistic > 3.84) { // Approximate critical value for p < 0.5       significantEffects.add("Significant condition effect (p < 0.5);         }
        }
        
        // Test for ITD effect
        List<ResponseData> positiveITD = responses.stream()
            .filter(r -> r.itd > 0)
            .collect(Collectors.toList());
        List<ResponseData> negativeITD = responses.stream()
            .filter(r -> r.itd < 0)
            .collect(Collectors.toList());
        
        if (!positiveITD.isEmpty() && !negativeITD.isEmpty()) {
            double positiveAcc = positiveITD.stream()
                .mapToDouble(r -> r.correct ? 1.0 : 0.0
                .average()
                .orElse(0.0);
            double negativeAcc = negativeITD.stream()
                .mapToDouble(r -> r.correct ? 1.0 : 0.0
                .average()
                .orElse(00      
            if (Math.abs(positiveAcc - negativeAcc) > 0.1)[object Object]       significantEffects.add("Significant ITD direction effect);         }
        }
        
        return significantEffects;
    }
    
    // Calculate F-statistic for condition comparison
    private static double calculateFStatistic(Map<String, List<ResponseData>> byCondition) {
        // Simplified F-test calculation
        double grandMean = byCondition.values().stream()
            .flatMap(List::stream)
            .mapToDouble(r -> r.correct ? 1.0 : 0          .average()
            .orElse(0.0);
        
        double betweenGroupSS = 00     double withinGroupSS = 0.0;
        
        for (List<ResponseData> group : byCondition.values()) {
            double groupMean = group.stream()
                .mapToDouble(r -> r.correct ? 1.0 : 0.0
                .average()
                .orElse(00      
            betweenGroupSS += group.size() * Math.pow(groupMean - grandMean,2      
            for (ResponseData response : group)[object Object]            double value = response.correct ? 1.0 : 0.0            withinGroupSS += Math.pow(value - groupMean, 2         }
        }
        
        double dfBetween = byCondition.size() -1
        double dfWithin = byCondition.values().stream().mapToInt(List::size).sum() - byCondition.size();
        
        if (dfWithin == 0) return 0.0;
        
        double msBetween = betweenGroupSS / dfBetween;
        double msWithin = withinGroupSS / dfWithin;
        
        return msBetween / msWithin;
    }
    
    // Generate summary
    private static String generateSummary(AnalysisResult result) {
        return String.format(N=%d, Accuracy=%0.1%%, 75%% Threshold=%.1fμs", 
            (int)(1.0 / (result.standardError * result.standardError)), // Approximate N
            result.meanAccuracy *100            result.confidenceInterval95 * 100,
            result.threshold75
    }
    
    // Helper methods
    private static double calculateMean(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0}
    
    private static double calculateStandardDeviation(List<Double> values) {
        double mean = calculateMean(values);
        double variance = values.stream()
            .mapToDouble(v -> Math.pow(v - mean,2          .average()
            .orElse(0.0       return Math.sqrt(variance);
    }
    
    // Response data class
    public static class ResponseData {
        public String participantId;
        public int trialNumber;
        public String stimulus;
        public double itd;
        public double ild;
        public double frequencyShift;
        public String response;
        public long responseTime;
        public boolean correct;
        public String condition;
    }
    
    // Generate publication-ready statistics
    public static void generatePublicationStats(String csvFile, String outputFile) throws IOException [object Object]    AnalysisResult result = analyzeResponses(csvFile);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.println("=== Publication-Ready Statistics ===");
            writer.println();
            writer.println("Method:");
            writer.println("Spatial hearing thresholds were measured using a two-alternative forced-choice");
            writer.println("paradigm. Participants indicated whether sounds appeared to originate from");
            writer.println(theleft or right side. Thresholds were calculated as the ITD value");
            writer.println(corresponding to 75% correct responses.");
            writer.println();
            writer.println("Results:");
            writer.printf("Mean accuracy across all conditions: %0.1f%% (SD = %.1f%%)\n", 
                result.meanAccuracy * 100result.standardDeviation *100            writer.printf("75%% correct threshold: %0.1f μs (95%% CI: ±%.1f μs)\n", 
                result.threshold75, result.confidenceInterval95 *100            writer.printf("50%% correct threshold: %.1f μs\n", result.threshold50);
            writer.println();
            
            if (!result.conditionMeans.isEmpty())[object Object]            writer.println("Condition-specific results:");
                for (Map.Entry<String, Double> entry : result.conditionMeans.entrySet()) {
                    writer.printf("%s: %.1%\n, entry.getKey(), entry.getValue() * 100);
                }
                writer.println();
            }
            
            if (!result.significantEffects.isEmpty())[object Object]            writer.println("Statistical significance:");
                for (String effect : result.significantEffects) {
                    writer.println("• " + effect);
                }
            }
        }
    }
} 