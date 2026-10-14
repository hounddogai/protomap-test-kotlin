package com.acme.transport.crewing.dutyperiod.estimate

/** A rule a adaptive duty period rollup must pass. Check returns its problems, or none. */
interface AdaptiveDutyPeriodRollupRule {
    fun check(item: AdaptiveDutyPeriodRollup): MutableList<String>
}

data class AdaptiveDutyPeriodRollupNameLengthRule(var maxLength: Int) : AdaptiveDutyPeriodRollupRule {
    override fun check(item: AdaptiveDutyPeriodRollup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDutyPeriodRollupCodeLengthRule(var maxLength: Int) : AdaptiveDutyPeriodRollupRule {
    override fun check(item: AdaptiveDutyPeriodRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDutyPeriodRollupCodePrefixRule(var prefix: String) : AdaptiveDutyPeriodRollupRule {
    override fun check(item: AdaptiveDutyPeriodRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive duty period rollup. */
data class AdaptiveDutyPeriodRollupValidator(var rules: MutableList<AdaptiveDutyPeriodRollupRule>) {
    fun validate(item: AdaptiveDutyPeriodRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveDutyPeriodRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveDutyPeriodRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveDutyPeriodRollupValidator(): AdaptiveDutyPeriodRollupValidator {
    return AdaptiveDutyPeriodRollupValidator(
        mutableListOf<AdaptiveDutyPeriodRollupRule>(
            AdaptiveDutyPeriodRollupNameLengthRule(3600),
            AdaptiveDutyPeriodRollupCodeLengthRule(3600),
            AdaptiveDutyPeriodRollupCodePrefixRule("REF"),
        ),
    )
}
