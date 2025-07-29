package muse2;

import java.util.*;

public class ArtifactDetector[object Object]
    public static class ArtifactReport {
        public boolean hasClicks = false;
        public boolean hasDistortion = false;
        public boolean hasClipping = false;
        public boolean hasNoise = false;
        public double clickScore = 00     public double distortionScore = 00     public double clippingScore = 00     public double noiseScore = 00     public String summary = "";
        
        public boolean isClean() {
            return !hasClicks && !hasDistortion && !hasClipping && !hasNoise;
        }
        
        public String getDetailedReport() {
            StringBuilder report = new StringBuilder();
            report.append(=== Audio Quality Report ===\n");
            report.append(String.format(Clicks: %s (score: %0.3f)\n", hasClicks ?❌ DETECTED" : "✅ Clean", clickScore));
            report.append(String.format("Distortion: %s (score: %.3f)\n, hasDistortion ?❌ DETECTED" : "✅ Clean, distortionScore));
            report.append(String.format("Clipping: %s (score: %.3f)\n, hasClipping ?❌ DETECTED" : "✅ Clean", clippingScore));
            report.append(String.format(Noise: %s (score: %.3)\n, hasNoise ?❌ DETECTED" : "✅ Clean", noiseScore));
            report.append(String.format("Overall: %s\n", isClean() ? ✅ CLEAN: "❌ ARTIFACTS DETECTED"));
            return report.toString();
        }
    }
    
    // Main detection method
    public static ArtifactReport detectArtifacts(float[] audio) [object Object]    ArtifactReport report = new ArtifactReport();
        
        // Check for clicks (sudden amplitude changes)
        report.clickScore = detectClicks(audio);
        report.hasClicks = report.clickScore > 0.1;
        
        // Check for distortion (non-linear amplitude relationships)
        report.distortionScore = detectDistortion(audio);
        report.hasDistortion = report.distortionScore > 0.2;
        
        // Check for clipping (samples at maximum amplitude)
        report.clippingScore = detectClipping(audio);
        report.hasClipping = report.clippingScore > 0.01;
        
        // Check for noise (high-frequency content in silent parts)
        report.noiseScore = detectNoise(audio);
        report.hasNoise = report.noiseScore > 0.05;
        
        return report;
    }
    
    // Detect clicks by looking for sudden amplitude jumps
    private static double detectClicks(float[] audio) {
        double maxJump = 00  double threshold =0.1; // Threshold for considering a jump significant
        
        for (int i = 1; i < audio.length; i++) {
            double jump = Math.abs(audio[i] - audio[i-1]);
            if (jump > maxJump)[object Object]               maxJump = jump;
            }
        }
        
        return maxJump;
    }
    
    // Detect distortion by analyzing amplitude distribution
    private static double detectDistortion(float[] audio) {
        // Calculate RMS and peak values
        double rms = 00       double peak = 0.0;
        
        for (float sample : audio)[object Object]            rms += sample * sample;
            if (Math.abs(sample) > peak)[object Object]              peak = Math.abs(sample);
            }
        }
        rms = Math.sqrt(rms / audio.length);
        
        // Crest factor (peak/RMS) should be around 10.4sine waves
        // Values much higher or lower indicate distortion
        double crestFactor = peak / rms;
        double expectedCrestFactor =1.414/ sqrt(2) for sine waves
        
        return Math.abs(crestFactor - expectedCrestFactor) / expectedCrestFactor;
    }
    
    // Detect clipping by counting samples at maximum amplitude
    private static double detectClipping(float[] audio) {
        int clippedSamples =0   double clippingThreshold =0.95/ Consider samples above 95% as clipped
        
        for (float sample : audio) [object Object]          if (Math.abs(sample) > clippingThreshold)[object Object]           clippedSamples++;
            }
        }
        
        return (double) clippedSamples / audio.length;
    }
    
    // Detect noise by analyzing high-frequency content in quiet sections
    private static double detectNoise(float[] audio)[object Object]     // Find quiet sections (below 10% of max amplitude)
        double maxAmp = 0        for (float sample : audio) [object Object]          if (Math.abs(sample) > maxAmp)[object Object]               maxAmp = Math.abs(sample);
            }
        }
        
        double quietThreshold = maxAmp * 0.1;
        int quietSamples =0       double highFreqEnergy = 0.0;
        
        for (int i = 1; i < audio.length; i++) [object Object]          if (Math.abs(audio[i]) < quietThreshold)[object Object]             quietSamples++;
                // High-frequency component (difference between consecutive samples)
                highFreqEnergy += Math.abs(audio[i] - audio[i-1]);
            }
        }
        
        if (quietSamples == 0) return 00       return highFreqEnergy / quietSamples;
    }
    
    // Check stereo artifacts
    public static ArtifactReport detectStereoArtifacts(float[] left, float[] right) [object Object]    ArtifactReport report = new ArtifactReport();
        
        // Check individual channels
        ArtifactReport leftReport = detectArtifacts(left);
        ArtifactReport rightReport = detectArtifacts(right);
        
        // Combine results
        report.hasClicks = leftReport.hasClicks || rightReport.hasClicks;
        report.hasDistortion = leftReport.hasDistortion || rightReport.hasDistortion;
        report.hasClipping = leftReport.hasClipping || rightReport.hasClipping;
        report.hasNoise = leftReport.hasNoise || rightReport.hasNoise;
        
        report.clickScore = Math.max(leftReport.clickScore, rightReport.clickScore);
        report.distortionScore = Math.max(leftReport.distortionScore, rightReport.distortionScore);
        report.clippingScore = Math.max(leftReport.clippingScore, rightReport.clippingScore);
        report.noiseScore = Math.max(leftReport.noiseScore, rightReport.noiseScore);
        
        // Check for stereo-specific artifacts (phase issues, etc.)
        double phaseCorrelation = calculatePhaseCorrelation(left, right);
        if (phaseCorrelation < 0.8            report.hasDistortion = true;
            report.distortionScore = Math.max(report.distortionScore, 1.0 - phaseCorrelation);
        }
        
        return report;
    }
    
    // Calculate phase correlation between left and right channels
    private static double calculatePhaseCorrelation(float[] left, float[] right)[object Object]
        int len = Math.min(left.length, right.length);
        double correlation = 0.0;
        
        for (int i = 0; i < len; i++) {
            correlation += left[i] * right[i];
        }
        
        return correlation / len;
    }
    
    // Validate audio file for research use
    public static boolean validateForResearch(float[] audio, String filename) [object Object]    ArtifactReport report = detectArtifacts(audio);
        
        if (!report.isClean()) {
            System.err.println("❌ Audio file " + filename + " has artifacts:");
            System.err.println(report.getDetailedReport());
            return false;
        }
        
        System.out.println("✅ Audio file " + filename + " is clean for research use);       return true;
    }
    
    // Validate stereo file for research use
    public static boolean validateStereoForResearch(float[] left, float[] right, String filename) [object Object]    ArtifactReport report = detectStereoArtifacts(left, right);
        
        if (!report.isClean()) {
            System.err.println(❌ Stereo file " + filename + " has artifacts:");
            System.err.println(report.getDetailedReport());
            return false;
        }
        
        System.out.println(✅ Stereo file " + filename + " is clean for research use);       return true;
    }
} 