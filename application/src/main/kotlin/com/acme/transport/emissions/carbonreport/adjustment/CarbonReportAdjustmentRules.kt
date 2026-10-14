package com.acme.transport.emissions.carbonreport.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a carbon report adjustment must pass. Check returns its problems, or none. */
interface CarbonReportAdjustmentRule {
    fun check(item: CarbonReportAdjustment): MutableList<String>
}

data class CarbonReportAdjustmentNameLengthRule(var maxLength: Int) : CarbonReportAdjustmentRule {
    override fun check(item: CarbonReportAdjustment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportAdjustmentRequiredCodeRule(var message: String) : CarbonReportAdjustmentRule {
    override fun check(item: CarbonReportAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportAdjustmentAllowedShiftCodeRule(
    var allowed: MutableList<String>,
) : CarbonReportAdjustmentRule {
    override fun check(item: CarbonReportAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportAdjustmentUpdatedAtRangeRule(var min: Int, var max: Int) : CarbonReportAdjustmentRule {
    override fun check(item: CarbonReportAdjustment): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class CarbonReportAdjustmentTrailerIdLengthRule(var maxLength: Int) : CarbonReportAdjustmentRule {
    override fun check(item: CarbonReportAdjustment): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report adjustment. */
data class CarbonReportAdjustmentValidator(var rules: MutableList<CarbonReportAdjustmentRule>) {
    fun validate(item: CarbonReportAdjustment): MutableList<String> {
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

    fun requireValid(item: CarbonReportAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportAdjustmentValidator(): CarbonReportAdjustmentValidator {
    return CarbonReportAdjustmentValidator(
        mutableListOf<CarbonReportAdjustmentRule>(
            CarbonReportAdjustmentNameLengthRule(32),
            CarbonReportAdjustmentRequiredCodeRule("carbon report adjustment needs a value"),
            CarbonReportAdjustmentAllowedShiftCodeRule(mutableListOf<String>()),
            CarbonReportAdjustmentUpdatedAtRangeRule(16, 255),
            CarbonReportAdjustmentTrailerIdLengthRule(64),
        ),
    )
}
