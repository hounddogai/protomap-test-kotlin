package com.acme.transport.fleetops.inspection.inspectionscore

/** A rule a weighted inspection score must pass. Check returns its problems, or none. */
interface WeightedInspectionScoreRule {
    fun check(item: WeightedInspectionScore): MutableList<String>
}

data class WeightedInspectionScoreCodePrefixRule(var prefix: String) : WeightedInspectionScoreRule {
    override fun check(item: WeightedInspectionScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionScoreActiveGuardRule(var limit: Int) : WeightedInspectionScoreRule {
    override fun check(item: WeightedInspectionScore): MutableList<String> {
        if (item.active && item.batchSize > limit) {
            return mutableListOf<String>("batch size ${item.batchSize} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionScoreCodeLengthRule(var maxLength: Int) : WeightedInspectionScoreRule {
    override fun check(item: WeightedInspectionScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionScoreVersionRangeRule(var min: Int, var max: Int) : WeightedInspectionScoreRule {
    override fun check(item: WeightedInspectionScore): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionScoreRequiredLaneRule(var message: String) : WeightedInspectionScoreRule {
    override fun check(item: WeightedInspectionScore): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted inspection score. */
data class WeightedInspectionScoreValidator(var rules: MutableList<WeightedInspectionScoreRule>) {
    fun validate(item: WeightedInspectionScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedInspectionScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedInspectionScore>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedInspectionScoreValidator(): WeightedInspectionScoreValidator {
    return WeightedInspectionScoreValidator(
        mutableListOf<WeightedInspectionScoreRule>(
            WeightedInspectionScoreCodePrefixRule("OPS"),
            WeightedInspectionScoreActiveGuardRule(255),
            WeightedInspectionScoreCodeLengthRule(86400),
            WeightedInspectionScoreVersionRangeRule(64, 86400),
            WeightedInspectionScoreRequiredLaneRule("weighted inspection score needs this field"),
        ),
    )
}
