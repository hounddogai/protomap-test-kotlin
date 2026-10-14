package com.acme.transport.emissions.carbonreport.rule

/** A rule a scheduled carbon report cycle must pass. Check returns its problems, or none. */
interface ScheduledCarbonReportCycleRule {
    fun check(item: ScheduledCarbonReportCycle): MutableList<String>
}

data class ScheduledCarbonReportCycleWeightRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledCarbonReportCycleRule {
    override fun check(item: ScheduledCarbonReportCycle): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class ScheduledCarbonReportCycleShiftCodeLengthRule(var maxLength: Int) : ScheduledCarbonReportCycleRule {
    override fun check(item: ScheduledCarbonReportCycle): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCarbonReportCycleRequiredSummaryRule(var message: String) : ScheduledCarbonReportCycleRule {
    override fun check(item: ScheduledCarbonReportCycle): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCarbonReportCycleSummaryLengthRule(var maxLength: Int) : ScheduledCarbonReportCycleRule {
    override fun check(item: ScheduledCarbonReportCycle): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCarbonReportCycleCodeLengthRule(var maxLength: Int) : ScheduledCarbonReportCycleRule {
    override fun check(item: ScheduledCarbonReportCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled carbon report cycle. */
data class ScheduledCarbonReportCycleValidator(var rules: MutableList<ScheduledCarbonReportCycleRule>) {
    fun validate(item: ScheduledCarbonReportCycle): MutableList<String> {
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

    fun requireValid(item: ScheduledCarbonReportCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledCarbonReportCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledCarbonReportCycleValidator(): ScheduledCarbonReportCycleValidator {
    return ScheduledCarbonReportCycleValidator(
        mutableListOf<ScheduledCarbonReportCycleRule>(
            ScheduledCarbonReportCycleWeightRangeRule(9.1, 1764.9),
            ScheduledCarbonReportCycleShiftCodeLengthRule(1000),
            ScheduledCarbonReportCycleRequiredSummaryRule("scheduled carbon report cycle needs an entry"),
            ScheduledCarbonReportCycleSummaryLengthRule(255),
            ScheduledCarbonReportCycleCodeLengthRule(32),
        ),
    )
}
