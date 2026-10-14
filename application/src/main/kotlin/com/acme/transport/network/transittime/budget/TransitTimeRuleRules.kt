package com.acme.transport.network.transittime.budget

/** A rule a transit time rule must pass. Check returns its problems, or none. */
interface TransitTimeRuleRule {
    fun check(item: TransitTimeRule): MutableList<String>
}

data class TransitTimeRuleActiveGuardRule(var limit: Int) : TransitTimeRuleRule {
    override fun check(item: TransitTimeRule): MutableList<String> {
        if (item.active && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeRuleCodePrefixRule(var prefix: String) : TransitTimeRuleRule {
    override fun check(item: TransitTimeRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeRuleRequiredCodeRule(var message: String) : TransitTimeRuleRule {
    override fun check(item: TransitTimeRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeRuleWindowDaysRangeRule(var min: Int, var max: Int) : TransitTimeRuleRule {
    override fun check(item: TransitTimeRule): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time rule. */
data class TransitTimeRuleValidator(var rules: MutableList<TransitTimeRuleRule>) {
    fun validate(item: TransitTimeRule): MutableList<String> {
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

    fun requireValid(item: TransitTimeRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeRule>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeRuleValidator(): TransitTimeRuleValidator {
    return TransitTimeRuleValidator(
        mutableListOf<TransitTimeRuleRule>(
            TransitTimeRuleActiveGuardRule(32),
            TransitTimeRuleCodePrefixRule("ACM"),
            TransitTimeRuleRequiredCodeRule("transit time rule needs this field"),
            TransitTimeRuleWindowDaysRangeRule(16, 64),
        ),
    )
}
