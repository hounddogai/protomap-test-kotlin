package com.acme.transport.emissions.carbonreport.reportthreshold

/** A rule a rolling carbon report cycle must pass. Check returns its problems, or none. */
interface RollingCarbonReportCycleRule {
    fun check(item: RollingCarbonReportCycle): MutableList<String>
}

data class RollingCarbonReportCycleCarrierLengthRule(var maxLength: Int) : RollingCarbonReportCycleRule {
    override fun check(item: RollingCarbonReportCycle): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCarbonReportCycleRequiredCodeRule(var message: String) : RollingCarbonReportCycleRule {
    override fun check(item: RollingCarbonReportCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingCarbonReportCycleRequiredNameRule(var message: String) : RollingCarbonReportCycleRule {
    override fun check(item: RollingCarbonReportCycle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingCarbonReportCycleRatioRangeRule(
    var min: Double,
    var max: Double,
) : RollingCarbonReportCycleRule {
    override fun check(item: RollingCarbonReportCycle): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class RollingCarbonReportCycleRequiredCarrierRule(var message: String) : RollingCarbonReportCycleRule {
    override fun check(item: RollingCarbonReportCycle): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingCarbonReportCycleCodeLengthRule(var maxLength: Int) : RollingCarbonReportCycleRule {
    override fun check(item: RollingCarbonReportCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling carbon report cycle. */
data class RollingCarbonReportCycleValidator(var rules: MutableList<RollingCarbonReportCycleRule>) {
    fun validate(item: RollingCarbonReportCycle): MutableList<String> {
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

    fun requireValid(item: RollingCarbonReportCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingCarbonReportCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingCarbonReportCycleValidator(): RollingCarbonReportCycleValidator {
    return RollingCarbonReportCycleValidator(
        mutableListOf<RollingCarbonReportCycleRule>(
            RollingCarbonReportCycleCarrierLengthRule(255),
            RollingCarbonReportCycleRequiredCodeRule("rolling carbon report cycle needs a value"),
            RollingCarbonReportCycleRequiredNameRule("rolling carbon report cycle needs a value"),
            RollingCarbonReportCycleRatioRangeRule(9.0, 2094.2),
            RollingCarbonReportCycleRequiredCarrierRule("rolling carbon report cycle needs a value"),
            RollingCarbonReportCycleCodeLengthRule(255),
        ),
    )
}
