package com.acme.transport.fleetops.utilization.utilizationsegment

/** A rule a weighted utilization segment must pass. Check returns its problems, or none. */
interface WeightedUtilizationSegmentRule {
    fun check(item: WeightedUtilizationSegment): MutableList<String>
}

data class WeightedUtilizationSegmentRequiredCodeRule(var message: String) : WeightedUtilizationSegmentRule {
    override fun check(item: WeightedUtilizationSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedUtilizationSegmentExternalRefLengthRule(
    var maxLength: Int,
) : WeightedUtilizationSegmentRule {
    override fun check(item: WeightedUtilizationSegment): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedUtilizationSegmentRequiredLabelRule(var message: String) : WeightedUtilizationSegmentRule {
    override fun check(item: WeightedUtilizationSegment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted utilization segment. */
data class WeightedUtilizationSegmentValidator(var rules: MutableList<WeightedUtilizationSegmentRule>) {
    fun validate(item: WeightedUtilizationSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedUtilizationSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedUtilizationSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedUtilizationSegmentValidator(): WeightedUtilizationSegmentValidator {
    return WeightedUtilizationSegmentValidator(
        mutableListOf<WeightedUtilizationSegmentRule>(
            WeightedUtilizationSegmentRequiredCodeRule("weighted utilization segment needs an entry"),
            WeightedUtilizationSegmentExternalRefLengthRule(255),
            WeightedUtilizationSegmentRequiredLabelRule("weighted utilization segment needs an entry"),
        ),
    )
}
