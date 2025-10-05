package org.mangorage.loader;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class JarConfig {
    @SerializedName("jars")
    private List<Jar> jars;

    public List<Jar> getJars() {
        return jars;
    }

    public static class Jar {
        private Identifier identifier;
        private Version version;
        private String path;
        private boolean isObfuscated;

        public Identifier getIdentifier() {
            return identifier;
        }

        public Version getVersion() {
            return version;
        }

        public String getPath() {
            return path;
        }

        public boolean isObfuscated() {
            return isObfuscated;
        }
    }

    public static class Identifier {
        private String group;
        private String artifact;

        public String getGroup() {
            return group;
        }

        public String getArtifact() {
            return artifact;
        }
    }

    public static class Version {
        private String range;
        private String artifactVersion;

        public String getRange() {
            return range;
        }

        public String getArtifactVersion() {
            return artifactVersion;
        }
    }
}
