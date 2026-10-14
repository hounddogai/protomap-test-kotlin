package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.kit.text.normalize

/** A rule a batched lease return rule must pass. Check returns its problems, or none. */
interface BatchedLeaseReturnRuleRule {
    fun check(item: BatchedLeaseReturnRule): MutableList<String>
}

data class BatchedLeaseReturnRuleCodeLengthRule(var maxLength: Int) : BatchedLeaseReturnRuleRule {
    override fun check(item: BatchedLeaseReturnRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedLeaseReturnRuleAllowedNameRule(
    var allowed: MutableList<String>,
) : BatchedLeaseReturnRuleRule {
    override fun check(item: BatchedLeaseReturnRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is too long")
        }
        return mutableListOf<String>()
    }
}

data class BatchedLeaseReturnRuleRequiredNameRule(var message: String) : BatchedLeaseReturnRuleRule {
    override fun check(item: BatchedLeaseReturnRule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched lease return rule. */
data class BatchedLeaseReturnRuleValidator(var rules: MutableList<BatchedLeaseReturnRuleRule>) {
    fun validate(item: BatchedLeaseReturnRule): MutableList<String> {
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

    fun requireValid(item: BatchedLeaseReturnRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedLeaseReturnRule>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedLeaseReturnRuleValidator(): BatchedLeaseReturnRuleValidator {
    return BatchedLeaseReturnRuleValidator(
        mutableListOf<BatchedLeaseReturnRuleRule>(
            BatchedLeaseReturnRuleCodeLengthRule(255),
            BatchedLeaseReturnRuleAllowedNameRule(mutableListOf<String>("strict")),
            BatchedLeaseReturnRuleRequiredNameRule("batched lease return rule needs a value"),
        ),
    )
}
