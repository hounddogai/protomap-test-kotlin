package com.acme.transport.fleetops.leasereturn.returntier

/** A rule a rolling lease return tier must pass. Check returns its problems, or none. */
interface RollingLeaseReturnTierRule {
    fun check(item: RollingLeaseReturnTier): MutableList<String>
}

data class RollingLeaseReturnTierNameLengthRule(var maxLength: Int) : RollingLeaseReturnTierRule {
    override fun check(item: RollingLeaseReturnTier): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingLeaseReturnTierSequenceRangeRule(var min: Int, var max: Int) : RollingLeaseReturnTierRule {
    override fun check(item: RollingLeaseReturnTier): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingLeaseReturnTierScoreRangeRule(var min: Double, var max: Double) : RollingLeaseReturnTierRule {
    override fun check(item: RollingLeaseReturnTier): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class RollingLeaseReturnTierCodePrefixRule(var prefix: String) : RollingLeaseReturnTierRule {
    override fun check(item: RollingLeaseReturnTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingLeaseReturnTierCodeLengthRule(var maxLength: Int) : RollingLeaseReturnTierRule {
    override fun check(item: RollingLeaseReturnTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingLeaseReturnTierEndsAtRangeRule(var min: Int, var max: Int) : RollingLeaseReturnTierRule {
    override fun check(item: RollingLeaseReturnTier): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rolling lease return tier. */
data class RollingLeaseReturnTierValidator(var rules: MutableList<RollingLeaseReturnTierRule>) {
    fun validate(item: RollingLeaseReturnTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingLeaseReturnTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingLeaseReturnTier>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingLeaseReturnTierValidator(): RollingLeaseReturnTierValidator {
    return RollingLeaseReturnTierValidator(
        mutableListOf<RollingLeaseReturnTierRule>(
            RollingLeaseReturnTierNameLengthRule(32),
            RollingLeaseReturnTierSequenceRangeRule(64, 80),
            RollingLeaseReturnTierScoreRangeRule(4.2, 4296.0),
            RollingLeaseReturnTierCodePrefixRule("X"),
            RollingLeaseReturnTierCodeLengthRule(64),
            RollingLeaseReturnTierEndsAtRangeRule(255, 86400),
        ),
    )
}
