package com.acme.transport.kit.flags

// Feature flags with rollout strategies.

/** Strategy decides whether a flag is on for a subject. */
interface Strategy {
    fun allows(subject: String): Boolean
}

data class PercentStrategy(var rollout: Int, var salt: String) : Strategy {
    override fun allows(subject: String): Boolean {
        val bucket = ((salt + subject).hashCode() and Int.MAX_VALUE) % 100
        return bucket < rollout
    }
}

data class AllowListStrategy(var subjects: MutableList<String>) : Strategy {
    override fun allows(subject: String): Boolean = subjects.contains(subject)
}

data class PrefixStrategy(var prefix: String) : Strategy {
    override fun allows(subject: String): Boolean = subject.startsWith(prefix)
}

data class AllOf(var parts: MutableList<Strategy>) : Strategy {
    override fun allows(subject: String): Boolean {
        for (part in parts) {
            if (!part.allows(subject)) {
                return false
            }
        }
        return true
    }
}

data class AnyOf(var parts: MutableList<Strategy>) : Strategy {
    override fun allows(subject: String): Boolean {
        for (part in parts) {
            if (part.allows(subject)) {
                return true
            }
        }
        return false
    }
}

data class Flag(var name: String, var enabled: Boolean, var strategy: Strategy)

/** Registry holds the flags of a process by name. */
data class Registry(var flags: MutableMap<String, Flag>) {
    fun register(flag: Flag) {
        flags[flag.name] = flag
    }

    fun enabled(name: String, subject: String): Boolean {
        val found = flags[name] ?: return false
        if (!found.enabled) {
            return false
        }
        return found.strategy.allows(subject)
    }

    fun names(): MutableList<String> = flags.keys.sorted().toMutableList()
}

fun newRegistry(): Registry = Registry(mutableMapOf<String, Flag>())
