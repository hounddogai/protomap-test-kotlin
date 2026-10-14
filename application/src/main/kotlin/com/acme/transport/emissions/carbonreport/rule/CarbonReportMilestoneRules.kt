package com.acme.transport.emissions.carbonreport.rule

/** A rule a carbon report milestone must pass. Check returns its problems, or none. */
interface CarbonReportMilestoneRule {
    fun check(item: CarbonReportMilestone): MutableList<String>
}

data class CarbonReportMilestoneRequiredTrailerIdRule(var message: String) : CarbonReportMilestoneRule {
    override fun check(item: CarbonReportMilestone): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportMilestoneLabelLengthRule(var maxLength: Int) : CarbonReportMilestoneRule {
    override fun check(item: CarbonReportMilestone): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportMilestoneGraceDaysRangeRule(var min: Int, var max: Int) : CarbonReportMilestoneRule {
    override fun check(item: CarbonReportMilestone): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportMilestoneTrailerIdLengthRule(var maxLength: Int) : CarbonReportMilestoneRule {
    override fun check(item: CarbonReportMilestone): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report milestone. */
data class CarbonReportMilestoneValidator(var rules: MutableList<CarbonReportMilestoneRule>) {
    fun validate(item: CarbonReportMilestone): MutableList<String> {
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

    fun requireValid(item: CarbonReportMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportMilestoneValidator(): CarbonReportMilestoneValidator {
    return CarbonReportMilestoneValidator(
        mutableListOf<CarbonReportMilestoneRule>(
            CarbonReportMilestoneRequiredTrailerIdRule("carbon report milestone needs a value"),
            CarbonReportMilestoneLabelLengthRule(32),
            CarbonReportMilestoneGraceDaysRangeRule(16, 16),
            CarbonReportMilestoneTrailerIdLengthRule(64),
        ),
    )
}
