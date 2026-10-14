package com.acme.transport.crewing.dutyperiod.mapping

/** A rule a legacy duty period mapping must pass. Check returns its problems, or none. */
interface LegacyDutyPeriodMappingRule {
    fun check(item: LegacyDutyPeriodMapping): MutableList<String>
}

data class LegacyDutyPeriodMappingTitleLengthRule(var maxLength: Int) : LegacyDutyPeriodMappingRule {
    override fun check(item: LegacyDutyPeriodMapping): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDutyPeriodMappingRequiredSummaryRule(var message: String) : LegacyDutyPeriodMappingRule {
    override fun check(item: LegacyDutyPeriodMapping): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDutyPeriodMappingRequiredCodeRule(var message: String) : LegacyDutyPeriodMappingRule {
    override fun check(item: LegacyDutyPeriodMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy duty period mapping. */
data class LegacyDutyPeriodMappingValidator(var rules: MutableList<LegacyDutyPeriodMappingRule>) {
    fun validate(item: LegacyDutyPeriodMapping): MutableList<String> {
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

    fun requireValid(item: LegacyDutyPeriodMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDutyPeriodMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDutyPeriodMappingValidator(): LegacyDutyPeriodMappingValidator {
    return LegacyDutyPeriodMappingValidator(
        mutableListOf<LegacyDutyPeriodMappingRule>(
            LegacyDutyPeriodMappingTitleLengthRule(120),
            LegacyDutyPeriodMappingRequiredSummaryRule("legacy duty period mapping needs an entry"),
            LegacyDutyPeriodMappingRequiredCodeRule("legacy duty period mapping needs a value"),
        ),
    )
}
