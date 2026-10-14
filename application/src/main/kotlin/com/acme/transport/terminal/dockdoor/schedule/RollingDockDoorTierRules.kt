package com.acme.transport.terminal.dockdoor.schedule

import com.acme.transport.kit.text.normalize

/** A rule a rolling dock door tier must pass. Check returns its problems, or none. */
interface RollingDockDoorTierRule {
    fun check(item: RollingDockDoorTier): MutableList<String>
}

data class RollingDockDoorTierAllowedChecksumRule(var allowed: MutableList<String>) : RollingDockDoorTierRule {
    override fun check(item: RollingDockDoorTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class RollingDockDoorTierScoreRangeRule(var min: Double, var max: Double) : RollingDockDoorTierRule {
    override fun check(item: RollingDockDoorTier): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingDockDoorTierRequiredChecksumRule(var message: String) : RollingDockDoorTierRule {
    override fun check(item: RollingDockDoorTier): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDockDoorTierRequiredCodeRule(var message: String) : RollingDockDoorTierRule {
    override fun check(item: RollingDockDoorTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling dock door tier. */
data class RollingDockDoorTierValidator(var rules: MutableList<RollingDockDoorTierRule>) {
    fun validate(item: RollingDockDoorTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingDockDoorTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDockDoorTier>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDockDoorTierValidator(): RollingDockDoorTierValidator {
    return RollingDockDoorTierValidator(
        mutableListOf<RollingDockDoorTierRule>(
            RollingDockDoorTierAllowedChecksumRule(mutableListOf<String>()),
            RollingDockDoorTierScoreRangeRule(5.6, 204.2),
            RollingDockDoorTierRequiredChecksumRule("rolling dock door tier needs this field"),
            RollingDockDoorTierRequiredCodeRule("rolling dock door tier needs an entry"),
        ),
    )
}
