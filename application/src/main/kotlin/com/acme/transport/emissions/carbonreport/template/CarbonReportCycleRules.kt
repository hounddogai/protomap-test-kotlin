package com.acme.transport.emissions.carbonreport.template

/** A rule a carbon report cycle must pass. Check returns its problems, or none. */
interface CarbonReportCycleRule {
    fun check(item: CarbonReportCycle): MutableList<String>
}

data class CarbonReportCycleCurrencyLengthRule(var maxLength: Int) : CarbonReportCycleRule {
    override fun check(item: CarbonReportCycle): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportCycleDriverIdLengthRule(var maxLength: Int) : CarbonReportCycleRule {
    override fun check(item: CarbonReportCycle): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportCycleLabelLengthRule(var maxLength: Int) : CarbonReportCycleRule {
    override fun check(item: CarbonReportCycle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportCycleRequiredCodeRule(var message: String) : CarbonReportCycleRule {
    override fun check(item: CarbonReportCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportCycleRequiredDriverIdRule(var message: String) : CarbonReportCycleRule {
    override fun check(item: CarbonReportCycle): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report cycle. */
data class CarbonReportCycleValidator(var rules: MutableList<CarbonReportCycleRule>) {
    fun validate(item: CarbonReportCycle): MutableList<String> {
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

    fun requireValid(item: CarbonReportCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportCycleValidator(): CarbonReportCycleValidator {
    return CarbonReportCycleValidator(
        mutableListOf<CarbonReportCycleRule>(
            CarbonReportCycleCurrencyLengthRule(1000),
            CarbonReportCycleDriverIdLengthRule(64),
            CarbonReportCycleLabelLengthRule(3600),
            CarbonReportCycleRequiredCodeRule("carbon report cycle needs a value"),
            CarbonReportCycleRequiredDriverIdRule("carbon report cycle needs a value"),
        ),
    )
}
