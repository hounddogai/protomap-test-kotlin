package com.acme.transport.emissions.carbonreport.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a tiered carbon report adjustment must pass. Check returns its problems, or none. */
interface TieredCarbonReportAdjustmentRule {
    fun check(item: TieredCarbonReportAdjustment): MutableList<String>
}

data class TieredCarbonReportAdjustmentCodeLengthRule(var maxLength: Int) : TieredCarbonReportAdjustmentRule {
    override fun check(item: TieredCarbonReportAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCarbonReportAdjustmentApprovedGuardRule(var limit: Double) : TieredCarbonReportAdjustmentRule {
    override fun check(item: TieredCarbonReportAdjustment): MutableList<String> {
        if (item.approved && item.rate > limit) {
            return mutableListOf<String>("rate ${item.rate} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class TieredCarbonReportAdjustmentDockLengthRule(var maxLength: Int) : TieredCarbonReportAdjustmentRule {
    override fun check(item: TieredCarbonReportAdjustment): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCarbonReportAdjustmentLabelLengthRule(var maxLength: Int) : TieredCarbonReportAdjustmentRule {
    override fun check(item: TieredCarbonReportAdjustment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCarbonReportAdjustmentCodePrefixRule(var prefix: String) : TieredCarbonReportAdjustmentRule {
    override fun check(item: TieredCarbonReportAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredCarbonReportAdjustmentAllowedCodeRule(
    var allowed: MutableList<String>,
) : TieredCarbonReportAdjustmentRule {
    override fun check(item: TieredCarbonReportAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered carbon report adjustment. */
data class TieredCarbonReportAdjustmentValidator(var rules: MutableList<TieredCarbonReportAdjustmentRule>) {
    fun validate(item: TieredCarbonReportAdjustment): MutableList<String> {
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

    fun requireValid(item: TieredCarbonReportAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCarbonReportAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCarbonReportAdjustmentValidator(): TieredCarbonReportAdjustmentValidator {
    return TieredCarbonReportAdjustmentValidator(
        mutableListOf<TieredCarbonReportAdjustmentRule>(
            TieredCarbonReportAdjustmentCodeLengthRule(3600),
            TieredCarbonReportAdjustmentApprovedGuardRule(3.4),
            TieredCarbonReportAdjustmentDockLengthRule(86400),
            TieredCarbonReportAdjustmentLabelLengthRule(16),
            TieredCarbonReportAdjustmentCodePrefixRule("OPS"),
            TieredCarbonReportAdjustmentAllowedCodeRule(mutableListOf<String>("lenient", "adaptive")),
        ),
    )
}
