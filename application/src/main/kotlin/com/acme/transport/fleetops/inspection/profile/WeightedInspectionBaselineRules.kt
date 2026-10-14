package com.acme.transport.fleetops.inspection.profile

/** A rule a weighted inspection baseline must pass. Check returns its problems, or none. */
interface WeightedInspectionBaselineRule {
    fun check(item: WeightedInspectionBaseline): MutableList<String>
}

data class WeightedInspectionBaselineRequiredChecksumRule(
    var message: String,
) : WeightedInspectionBaselineRule {
    override fun check(item: WeightedInspectionBaseline): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionBaselineReviewerLengthRule(var maxLength: Int) : WeightedInspectionBaselineRule {
    override fun check(item: WeightedInspectionBaseline): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionBaselineRequiredReviewerRule(
    var message: String,
) : WeightedInspectionBaselineRule {
    override fun check(item: WeightedInspectionBaseline): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionBaselineLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : WeightedInspectionBaselineRule {
    override fun check(item: WeightedInspectionBaseline): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionBaselineLabelLengthRule(var maxLength: Int) : WeightedInspectionBaselineRule {
    override fun check(item: WeightedInspectionBaseline): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionBaselineChecksumLengthRule(var maxLength: Int) : WeightedInspectionBaselineRule {
    override fun check(item: WeightedInspectionBaseline): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted inspection baseline. */
data class WeightedInspectionBaselineValidator(var rules: MutableList<WeightedInspectionBaselineRule>) {
    fun validate(item: WeightedInspectionBaseline): MutableList<String> {
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

    fun requireValid(item: WeightedInspectionBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedInspectionBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedInspectionBaselineValidator(): WeightedInspectionBaselineValidator {
    return WeightedInspectionBaselineValidator(
        mutableListOf<WeightedInspectionBaselineRule>(
            WeightedInspectionBaselineRequiredChecksumRule("weighted inspection baseline needs this field"),
            WeightedInspectionBaselineReviewerLengthRule(3600),
            WeightedInspectionBaselineRequiredReviewerRule("weighted inspection baseline needs an entry"),
            WeightedInspectionBaselineLoadKgRangeRule(3.6, 2356.4),
            WeightedInspectionBaselineLabelLengthRule(120),
            WeightedInspectionBaselineChecksumLengthRule(64),
        ),
    )
}
