package com.acme.transport.emissions.carbonreport.reportbudget

/** A rule a manual carbon report cycle must pass. Check returns its problems, or none. */
interface ManualCarbonReportCycleRule {
    fun check(item: ManualCarbonReportCycle): MutableList<String>
}

data class ManualCarbonReportCycleCodePrefixRule(var prefix: String) : ManualCarbonReportCycleRule {
    override fun check(item: ManualCarbonReportCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualCarbonReportCycleRequiredDriverIdRule(var message: String) : ManualCarbonReportCycleRule {
    override fun check(item: ManualCarbonReportCycle): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualCarbonReportCycleStopCountRangeRule(var min: Int, var max: Int) : ManualCarbonReportCycleRule {
    override fun check(item: ManualCarbonReportCycle): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual carbon report cycle. */
data class ManualCarbonReportCycleValidator(var rules: MutableList<ManualCarbonReportCycleRule>) {
    fun validate(item: ManualCarbonReportCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualCarbonReportCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualCarbonReportCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultManualCarbonReportCycleValidator(): ManualCarbonReportCycleValidator {
    return ManualCarbonReportCycleValidator(
        mutableListOf<ManualCarbonReportCycleRule>(
            ManualCarbonReportCycleCodePrefixRule("X"),
            ManualCarbonReportCycleRequiredDriverIdRule("manual carbon report cycle needs a value"),
            ManualCarbonReportCycleStopCountRangeRule(80, 3600),
        ),
    )
}
