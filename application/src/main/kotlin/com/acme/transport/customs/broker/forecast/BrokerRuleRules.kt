package com.acme.transport.customs.broker.forecast

/** A rule a broker rule must pass. Check returns its problems, or none. */
interface BrokerRuleRule {
    fun check(item: BrokerRule): MutableList<String>
}

data class BrokerRuleCodeLengthRule(var maxLength: Int) : BrokerRuleRule {
    override fun check(item: BrokerRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerRuleFloorValueRangeRule(var min: Double, var max: Double) : BrokerRuleRule {
    override fun check(item: BrokerRule): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class BrokerRuleContactEmailLengthRule(var maxLength: Int) : BrokerRuleRule {
    override fun check(item: BrokerRule): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerRuleCodePrefixRule(var prefix: String) : BrokerRuleRule {
    override fun check(item: BrokerRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker rule. */
data class BrokerRuleValidator(var rules: MutableList<BrokerRuleRule>) {
    fun validate(item: BrokerRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BrokerRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerRule>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerRuleValidator(): BrokerRuleValidator {
    return BrokerRuleValidator(
        mutableListOf<BrokerRuleRule>(
            BrokerRuleCodeLengthRule(120),
            BrokerRuleFloorValueRangeRule(0.7, 3051.1),
            BrokerRuleContactEmailLengthRule(32),
            BrokerRuleCodePrefixRule("ACM"),
        ),
    )
}
