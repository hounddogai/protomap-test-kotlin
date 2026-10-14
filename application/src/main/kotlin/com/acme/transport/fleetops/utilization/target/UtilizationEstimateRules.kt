package com.acme.transport.fleetops.utilization.target

/** A rule a utilization estimate must pass. Check returns its problems, or none. */
interface UtilizationEstimateRule {
    fun check(item: UtilizationEstimate): MutableList<String>
}

data class UtilizationEstimateTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : UtilizationEstimateRule {
    override fun check(item: UtilizationEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class UtilizationEstimateAttemptsRangeRule(var min: Int, var max: Int) : UtilizationEstimateRule {
    override fun check(item: UtilizationEstimate): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationEstimateCodeLengthRule(var maxLength: Int) : UtilizationEstimateRule {
    override fun check(item: UtilizationEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationEstimateNameLengthRule(var maxLength: Int) : UtilizationEstimateRule {
    override fun check(item: UtilizationEstimate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization estimate. */
data class UtilizationEstimateValidator(var rules: MutableList<UtilizationEstimateRule>) {
    fun validate(item: UtilizationEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationEstimateValidator(): UtilizationEstimateValidator {
    return UtilizationEstimateValidator(
        mutableListOf<UtilizationEstimateRule>(
            UtilizationEstimateTagsLimitRule(120, mutableListOf<String>("manual", "global")),
            UtilizationEstimateAttemptsRangeRule(32, 64),
            UtilizationEstimateCodeLengthRule(1000),
            UtilizationEstimateNameLengthRule(80),
        ),
    )
}
