package com.acme.transport.crewing.dutyperiod.schedule

/** A rule a default duty period rule must pass. Check returns its problems, or none. */
interface DefaultDutyPeriodRuleRule {
    fun check(item: DefaultDutyPeriodRule): MutableList<String>
}

data class DefaultDutyPeriodRuleCodeLengthRule(var maxLength: Int) : DefaultDutyPeriodRuleRule {
    override fun check(item: DefaultDutyPeriodRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDutyPeriodRuleRateRangeRule(var min: Double, var max: Double) : DefaultDutyPeriodRuleRule {
    override fun check(item: DefaultDutyPeriodRule): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDutyPeriodRuleRequiredNameRule(var message: String) : DefaultDutyPeriodRuleRule {
    override fun check(item: DefaultDutyPeriodRule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultDutyPeriodRuleCodePrefixRule(var prefix: String) : DefaultDutyPeriodRuleRule {
    override fun check(item: DefaultDutyPeriodRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDutyPeriodRuleQuantityRangeRule(var min: Int, var max: Int) : DefaultDutyPeriodRuleRule {
    override fun check(item: DefaultDutyPeriodRule): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class DefaultDutyPeriodRuleChecksumLengthRule(var maxLength: Int) : DefaultDutyPeriodRuleRule {
    override fun check(item: DefaultDutyPeriodRule): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default duty period rule. */
data class DefaultDutyPeriodRuleValidator(var rules: MutableList<DefaultDutyPeriodRuleRule>) {
    fun validate(item: DefaultDutyPeriodRule): MutableList<String> {
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

    fun requireValid(item: DefaultDutyPeriodRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultDutyPeriodRule>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultDutyPeriodRuleValidator(): DefaultDutyPeriodRuleValidator {
    return DefaultDutyPeriodRuleValidator(
        mutableListOf<DefaultDutyPeriodRuleRule>(
            DefaultDutyPeriodRuleCodeLengthRule(86400),
            DefaultDutyPeriodRuleRateRangeRule(9.2, 4663.3),
            DefaultDutyPeriodRuleRequiredNameRule("default duty period rule needs this field"),
            DefaultDutyPeriodRuleCodePrefixRule("X"),
            DefaultDutyPeriodRuleQuantityRangeRule(255, 86400),
            DefaultDutyPeriodRuleChecksumLengthRule(120),
        ),
    )
}
