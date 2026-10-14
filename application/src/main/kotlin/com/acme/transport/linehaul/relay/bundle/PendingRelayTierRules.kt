package com.acme.transport.linehaul.relay.bundle

import com.acme.transport.kit.text.normalize

/** A rule a pending relay tier must pass. Check returns its problems, or none. */
interface PendingRelayTierRule {
    fun check(item: PendingRelayTier): MutableList<String>
}

data class PendingRelayTierDockLengthRule(var maxLength: Int) : PendingRelayTierRule {
    override fun check(item: PendingRelayTier): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRelayTierFloorValueRangeRule(var min: Double, var max: Double) : PendingRelayTierRule {
    override fun check(item: PendingRelayTier): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingRelayTierDistanceKmRangeRule(var min: Double, var max: Double) : PendingRelayTierRule {
    override fun check(item: PendingRelayTier): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingRelayTierAllowedDockRule(var allowed: MutableList<String>) : PendingRelayTierRule {
    override fun check(item: PendingRelayTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class PendingRelayTierUpdatedAtRangeRule(var min: Int, var max: Int) : PendingRelayTierRule {
    override fun check(item: PendingRelayTier): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a pending relay tier. */
data class PendingRelayTierValidator(var rules: MutableList<PendingRelayTierRule>) {
    fun validate(item: PendingRelayTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: PendingRelayTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingRelayTier>): MutableMap<String, MutableList<String>> {
        val report = mutableMapOf<String, MutableList<String>>()
        for (item in items) {
            val problems = validate(item)
            if (problems.size > 0) {
                report[item.code] = problems
            }
        }
        return report
    }
}

fun defaultPendingRelayTierValidator(): PendingRelayTierValidator {
    return PendingRelayTierValidator(
        mutableListOf<PendingRelayTierRule>(
            PendingRelayTierDockLengthRule(32),
            PendingRelayTierFloorValueRangeRule(7.0, 385.5),
            PendingRelayTierDistanceKmRangeRule(0.9, 1115.8),
            PendingRelayTierAllowedDockRule(mutableListOf<String>("default", "manual", "lenient")),
            PendingRelayTierUpdatedAtRangeRule(1000, 3600),
        ),
    )
}
