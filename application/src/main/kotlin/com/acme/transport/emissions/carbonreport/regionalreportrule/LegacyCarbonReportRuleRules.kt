package com.acme.transport.emissions.carbonreport.regionalreportrule

/** A rule a legacy carbon report rule must pass. Check returns its problems, or none. */
interface LegacyCarbonReportRuleRule {
    fun check(item: LegacyCarbonReportRule): MutableList<String>
}

data class LegacyCarbonReportRuleLimitRangeRule(var min: Int, var max: Int) : LegacyCarbonReportRuleRule {
    override fun check(item: LegacyCarbonReportRule): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class LegacyCarbonReportRuleShiftCodeLengthRule(var maxLength: Int) : LegacyCarbonReportRuleRule {
    override fun check(item: LegacyCarbonReportRule): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCarbonReportRuleEtaMinutesRangeRule(var min: Int, var max: Int) : LegacyCarbonReportRuleRule {
    override fun check(item: LegacyCarbonReportRule): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCarbonReportRuleWeightRangeRule(
    var min: Double,
    var max: Double,
) : LegacyCarbonReportRuleRule {
    override fun check(item: LegacyCarbonReportRule): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCarbonReportRuleRequiredShiftCodeRule(var message: String) : LegacyCarbonReportRuleRule {
    override fun check(item: LegacyCarbonReportRule): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy carbon report rule. */
data class LegacyCarbonReportRuleValidator(var rules: MutableList<LegacyCarbonReportRuleRule>) {
    fun validate(item: LegacyCarbonReportRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyCarbonReportRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyCarbonReportRule>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyCarbonReportRuleValidator(): LegacyCarbonReportRuleValidator {
    return LegacyCarbonReportRuleValidator(
        mutableListOf<LegacyCarbonReportRuleRule>(
            LegacyCarbonReportRuleLimitRangeRule(120, 86400),
            LegacyCarbonReportRuleShiftCodeLengthRule(86400),
            LegacyCarbonReportRuleEtaMinutesRangeRule(1000, 3600),
            LegacyCarbonReportRuleWeightRangeRule(6.9, 3739.5),
            LegacyCarbonReportRuleRequiredShiftCodeRule("legacy carbon report rule needs an entry"),
        ),
    )
}
