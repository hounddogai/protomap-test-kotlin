package com.acme.transport.network.node.tier

/** A rule a strict node tier must pass. Check returns its problems, or none. */
interface StrictNodeTierRule {
    fun check(item: StrictNodeTier): MutableList<String>
}

data class StrictNodeTierApprovedGuardRule(var limit: Double) : StrictNodeTierRule {
    override fun check(item: StrictNodeTier): MutableList<String> {
        if (item.approved && item.floorValue > limit) {
            return mutableListOf<String>("floor value ${item.floorValue} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class StrictNodeTierTitleLengthRule(var maxLength: Int) : StrictNodeTierRule {
    override fun check(item: StrictNodeTier): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictNodeTierRequiredTitleRule(var message: String) : StrictNodeTierRule {
    override fun check(item: StrictNodeTier): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictNodeTierMaximumRangeRule(var min: Double, var max: Double) : StrictNodeTierRule {
    override fun check(item: StrictNodeTier): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class StrictNodeTierCodeLengthRule(var maxLength: Int) : StrictNodeTierRule {
    override fun check(item: StrictNodeTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictNodeTierRequiredCodeRule(var message: String) : StrictNodeTierRule {
    override fun check(item: StrictNodeTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict node tier. */
data class StrictNodeTierValidator(var rules: MutableList<StrictNodeTierRule>) {
    fun validate(item: StrictNodeTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictNodeTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictNodeTier>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictNodeTierValidator(): StrictNodeTierValidator {
    return StrictNodeTierValidator(
        mutableListOf<StrictNodeTierRule>(
            StrictNodeTierApprovedGuardRule(0.8),
            StrictNodeTierTitleLengthRule(255),
            StrictNodeTierRequiredTitleRule("strict node tier needs this field"),
            StrictNodeTierMaximumRangeRule(3.2, 3097.5),
            StrictNodeTierCodeLengthRule(64),
            StrictNodeTierRequiredCodeRule("strict node tier needs a value"),
        ),
    )
}
