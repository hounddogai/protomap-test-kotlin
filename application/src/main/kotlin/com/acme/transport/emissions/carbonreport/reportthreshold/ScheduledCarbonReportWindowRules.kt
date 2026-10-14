package com.acme.transport.emissions.carbonreport.reportthreshold

/** A rule a scheduled carbon report window must pass. Check returns its problems, or none. */
interface ScheduledCarbonReportWindowRule {
    fun check(item: ScheduledCarbonReportWindow): MutableList<String>
}

data class ScheduledCarbonReportWindowTitleLengthRule(var maxLength: Int) : ScheduledCarbonReportWindowRule {
    override fun check(item: ScheduledCarbonReportWindow): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCarbonReportWindowRegionLengthRule(var maxLength: Int) : ScheduledCarbonReportWindowRule {
    override fun check(item: ScheduledCarbonReportWindow): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCarbonReportWindowRequiredRegionRule(
    var message: String,
) : ScheduledCarbonReportWindowRule {
    override fun check(item: ScheduledCarbonReportWindow): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCarbonReportWindowGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledCarbonReportWindowRule {
    override fun check(item: ScheduledCarbonReportWindow): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class ScheduledCarbonReportWindowContactEmailLengthRule(
    var maxLength: Int,
) : ScheduledCarbonReportWindowRule {
    override fun check(item: ScheduledCarbonReportWindow): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled carbon report window. */
data class ScheduledCarbonReportWindowValidator(var rules: MutableList<ScheduledCarbonReportWindowRule>) {
    fun validate(item: ScheduledCarbonReportWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledCarbonReportWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledCarbonReportWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledCarbonReportWindowValidator(): ScheduledCarbonReportWindowValidator {
    return ScheduledCarbonReportWindowValidator(
        mutableListOf<ScheduledCarbonReportWindowRule>(
            ScheduledCarbonReportWindowTitleLengthRule(120),
            ScheduledCarbonReportWindowRegionLengthRule(255),
            ScheduledCarbonReportWindowRequiredRegionRule("scheduled carbon report window needs an entry"),
            ScheduledCarbonReportWindowGraceDaysRangeRule(80, 1000),
            ScheduledCarbonReportWindowContactEmailLengthRule(3600),
        ),
    )
}
