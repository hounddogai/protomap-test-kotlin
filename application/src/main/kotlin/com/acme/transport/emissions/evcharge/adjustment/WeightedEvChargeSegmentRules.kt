package com.acme.transport.emissions.evcharge.adjustment

/** A rule a weighted ev charge segment must pass. Check returns its problems, or none. */
interface WeightedEvChargeSegmentRule {
    fun check(item: WeightedEvChargeSegment): MutableList<String>
}

data class WeightedEvChargeSegmentSummaryLengthRule(var maxLength: Int) : WeightedEvChargeSegmentRule {
    override fun check(item: WeightedEvChargeSegment): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedEvChargeSegmentRequiredCodeRule(var message: String) : WeightedEvChargeSegmentRule {
    override fun check(item: WeightedEvChargeSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedEvChargeSegmentDescriptionLengthRule(var maxLength: Int) : WeightedEvChargeSegmentRule {
    override fun check(item: WeightedEvChargeSegment): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted ev charge segment. */
data class WeightedEvChargeSegmentValidator(var rules: MutableList<WeightedEvChargeSegmentRule>) {
    fun validate(item: WeightedEvChargeSegment): MutableList<String> {
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

    fun requireValid(item: WeightedEvChargeSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedEvChargeSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedEvChargeSegmentValidator(): WeightedEvChargeSegmentValidator {
    return WeightedEvChargeSegmentValidator(
        mutableListOf<WeightedEvChargeSegmentRule>(
            WeightedEvChargeSegmentSummaryLengthRule(80),
            WeightedEvChargeSegmentRequiredCodeRule("weighted ev charge segment needs this field"),
            WeightedEvChargeSegmentDescriptionLengthRule(255),
        ),
    )
}
