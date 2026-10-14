package com.acme.transport.fleetops.tire.tiremanifest

/** A rule a weighted tire milestone must pass. Check returns its problems, or none. */
interface WeightedTireMilestoneRule {
    fun check(item: WeightedTireMilestone): MutableList<String>
}

data class WeightedTireMilestoneRevisionRangeRule(var min: Int, var max: Int) : WeightedTireMilestoneRule {
    override fun check(item: WeightedTireMilestone): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTireMilestoneRequiredCodeRule(var message: String) : WeightedTireMilestoneRule {
    override fun check(item: WeightedTireMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedTireMilestoneCodePrefixRule(var prefix: String) : WeightedTireMilestoneRule {
    override fun check(item: WeightedTireMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTireMilestoneLoadKgRangeRule(var min: Double, var max: Double) : WeightedTireMilestoneRule {
    override fun check(item: WeightedTireMilestone): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class WeightedTireMilestoneCodeLengthRule(var maxLength: Int) : WeightedTireMilestoneRule {
    override fun check(item: WeightedTireMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted tire milestone. */
data class WeightedTireMilestoneValidator(var rules: MutableList<WeightedTireMilestoneRule>) {
    fun validate(item: WeightedTireMilestone): MutableList<String> {
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

    fun requireValid(item: WeightedTireMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedTireMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedTireMilestoneValidator(): WeightedTireMilestoneValidator {
    return WeightedTireMilestoneValidator(
        mutableListOf<WeightedTireMilestoneRule>(
            WeightedTireMilestoneRevisionRangeRule(16, 3600),
            WeightedTireMilestoneRequiredCodeRule("weighted tire milestone needs an entry"),
            WeightedTireMilestoneCodePrefixRule("OPS"),
            WeightedTireMilestoneLoadKgRangeRule(7.2, 2261.8),
            WeightedTireMilestoneCodeLengthRule(255),
        ),
    )
}
