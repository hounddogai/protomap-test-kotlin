package com.acme.transport.emissions.carbonreport.mapping

import com.acme.transport.kit.text.normalize

/** A rule a legacy carbon report mapping must pass. Check returns its problems, or none. */
interface LegacyCarbonReportMappingRule {
    fun check(item: LegacyCarbonReportMapping): MutableList<String>
}

data class LegacyCarbonReportMappingAllowedChannelRule(
    var allowed: MutableList<String>,
) : LegacyCarbonReportMappingRule {
    override fun check(item: LegacyCarbonReportMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCarbonReportMappingCapacityRangeRule(
    var min: Int,
    var max: Int,
) : LegacyCarbonReportMappingRule {
    override fun check(item: LegacyCarbonReportMapping): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class LegacyCarbonReportMappingTitleLengthRule(var maxLength: Int) : LegacyCarbonReportMappingRule {
    override fun check(item: LegacyCarbonReportMapping): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCarbonReportMappingRequiredCodeRule(var message: String) : LegacyCarbonReportMappingRule {
    override fun check(item: LegacyCarbonReportMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyCarbonReportMappingCeilingRangeRule(
    var min: Double,
    var max: Double,
) : LegacyCarbonReportMappingRule {
    override fun check(item: LegacyCarbonReportMapping): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy carbon report mapping. */
data class LegacyCarbonReportMappingValidator(var rules: MutableList<LegacyCarbonReportMappingRule>) {
    fun validate(item: LegacyCarbonReportMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyCarbonReportMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyCarbonReportMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyCarbonReportMappingValidator(): LegacyCarbonReportMappingValidator {
    return LegacyCarbonReportMappingValidator(
        mutableListOf<LegacyCarbonReportMappingRule>(
            LegacyCarbonReportMappingAllowedChannelRule(
                mutableListOf<String>("primary", "seasonal", "regional"),
            ),
            LegacyCarbonReportMappingCapacityRangeRule(120, 86400),
            LegacyCarbonReportMappingTitleLengthRule(80),
            LegacyCarbonReportMappingRequiredCodeRule("legacy carbon report mapping needs an entry"),
            LegacyCarbonReportMappingCeilingRangeRule(8.3, 3579.9),
        ),
    )
}
