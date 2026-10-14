package com.acme.transport.customs.broker.threshold

/** A rule a batched broker rule must pass. Check returns its problems, or none. */
interface BatchedBrokerRuleRule {
    fun check(item: BatchedBrokerRule): MutableList<String>
}

data class BatchedBrokerRuleCodePrefixRule(var prefix: String) : BatchedBrokerRuleRule {
    override fun check(item: BatchedBrokerRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedBrokerRuleLabelLengthRule(var maxLength: Int) : BatchedBrokerRuleRule {
    override fun check(item: BatchedBrokerRule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedBrokerRuleCodeLengthRule(var maxLength: Int) : BatchedBrokerRuleRule {
    override fun check(item: BatchedBrokerRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedBrokerRuleRequiredTimezoneRule(var message: String) : BatchedBrokerRuleRule {
    override fun check(item: BatchedBrokerRule): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched broker rule. */
data class BatchedBrokerRuleValidator(var rules: MutableList<BatchedBrokerRuleRule>) {
    fun validate(item: BatchedBrokerRule): MutableList<String> {
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

    fun requireValid(item: BatchedBrokerRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedBrokerRule>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedBrokerRuleValidator(): BatchedBrokerRuleValidator {
    return BatchedBrokerRuleValidator(
        mutableListOf<BatchedBrokerRuleRule>(
            BatchedBrokerRuleCodePrefixRule("X"),
            BatchedBrokerRuleLabelLengthRule(120),
            BatchedBrokerRuleCodeLengthRule(255),
            BatchedBrokerRuleRequiredTimezoneRule("batched broker rule needs this field"),
        ),
    )
}
