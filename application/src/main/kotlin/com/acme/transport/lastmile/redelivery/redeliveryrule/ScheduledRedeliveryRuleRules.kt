package com.acme.transport.lastmile.redelivery.redeliveryrule

/** A rule a scheduled redelivery rule must pass. Check returns its problems, or none. */
interface ScheduledRedeliveryRuleRule {
    fun check(item: ScheduledRedeliveryRule): MutableList<String>
}

data class ScheduledRedeliveryRuleShiftCodeLengthRule(var maxLength: Int) : ScheduledRedeliveryRuleRule {
    override fun check(item: ScheduledRedeliveryRule): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryRuleCodeLengthRule(var maxLength: Int) : ScheduledRedeliveryRuleRule {
    override fun check(item: ScheduledRedeliveryRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryRuleRequiredShiftCodeRule(var message: String) : ScheduledRedeliveryRuleRule {
    override fun check(item: ScheduledRedeliveryRule): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryRuleCodePrefixRule(var prefix: String) : ScheduledRedeliveryRuleRule {
    override fun check(item: ScheduledRedeliveryRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled redelivery rule. */
data class ScheduledRedeliveryRuleValidator(var rules: MutableList<ScheduledRedeliveryRuleRule>) {
    fun validate(item: ScheduledRedeliveryRule): MutableList<String> {
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

    fun requireValid(item: ScheduledRedeliveryRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRedeliveryRule>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRedeliveryRuleValidator(): ScheduledRedeliveryRuleValidator {
    return ScheduledRedeliveryRuleValidator(
        mutableListOf<ScheduledRedeliveryRuleRule>(
            ScheduledRedeliveryRuleShiftCodeLengthRule(16),
            ScheduledRedeliveryRuleCodeLengthRule(120),
            ScheduledRedeliveryRuleRequiredShiftCodeRule("scheduled redelivery rule needs this field"),
            ScheduledRedeliveryRuleCodePrefixRule("X"),
        ),
    )
}
