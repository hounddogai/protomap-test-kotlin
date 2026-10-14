package com.acme.transport.crewing.availability.rule

/** A rule a availability rule must pass. Check returns its problems, or none. */
interface AvailabilityRuleRule {
    fun check(item: AvailabilityRule): MutableList<String>
}

data class AvailabilityRuleRequiredTitleRule(var message: String) : AvailabilityRuleRule {
    override fun check(item: AvailabilityRule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityRuleBudgetRangeRule(var min: Double, var max: Double) : AvailabilityRuleRule {
    override fun check(item: AvailabilityRule): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityRuleCodeLengthRule(var maxLength: Int) : AvailabilityRuleRule {
    override fun check(item: AvailabilityRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityRuleCodePrefixRule(var prefix: String) : AvailabilityRuleRule {
    override fun check(item: AvailabilityRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityRuleLabelLengthRule(var maxLength: Int) : AvailabilityRuleRule {
    override fun check(item: AvailabilityRule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability rule. */
data class AvailabilityRuleValidator(var rules: MutableList<AvailabilityRuleRule>) {
    fun validate(item: AvailabilityRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AvailabilityRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityRule>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityRuleValidator(): AvailabilityRuleValidator {
    return AvailabilityRuleValidator(
        mutableListOf<AvailabilityRuleRule>(
            AvailabilityRuleRequiredTitleRule("availability rule needs this field"),
            AvailabilityRuleBudgetRangeRule(2.8, 3485.0),
            AvailabilityRuleCodeLengthRule(3600),
            AvailabilityRuleCodePrefixRule("Q"),
            AvailabilityRuleLabelLengthRule(1000),
        ),
    )
}
