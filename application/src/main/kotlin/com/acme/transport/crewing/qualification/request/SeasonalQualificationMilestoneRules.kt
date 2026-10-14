package com.acme.transport.crewing.qualification.request

/** A rule a seasonal qualification milestone must pass. Check returns its problems, or none. */
interface SeasonalQualificationMilestoneRule {
    fun check(item: SeasonalQualificationMilestone): MutableList<String>
}

data class SeasonalQualificationMilestoneLabelLengthRule(
    var maxLength: Int,
) : SeasonalQualificationMilestoneRule {
    override fun check(item: SeasonalQualificationMilestone): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalQualificationMilestoneBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalQualificationMilestoneRule {
    override fun check(item: SeasonalQualificationMilestone): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class SeasonalQualificationMilestoneRequiredLocaleRule(
    var message: String,
) : SeasonalQualificationMilestoneRule {
    override fun check(item: SeasonalQualificationMilestone): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal qualification milestone. */
data class SeasonalQualificationMilestoneValidator(var rules: MutableList<SeasonalQualificationMilestoneRule>) {
    fun validate(item: SeasonalQualificationMilestone): MutableList<String> {
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

    fun requireValid(item: SeasonalQualificationMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalQualificationMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalQualificationMilestoneValidator(): SeasonalQualificationMilestoneValidator {
    return SeasonalQualificationMilestoneValidator(
        mutableListOf<SeasonalQualificationMilestoneRule>(
            SeasonalQualificationMilestoneLabelLengthRule(32),
            SeasonalQualificationMilestoneBatchSizeRangeRule(16, 64),
            SeasonalQualificationMilestoneRequiredLocaleRule("seasonal qualification milestone needs a value"),
        ),
    )
}
