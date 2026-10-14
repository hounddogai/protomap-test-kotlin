package com.acme.transport.freightpay.audit.batch

/** A rule a audit rule must pass. Check returns its problems, or none. */
interface AuditRuleRule {
    fun check(item: AuditRule): MutableList<String>
}

data class AuditRuleCodePrefixRule(var prefix: String) : AuditRuleRule {
    override fun check(item: AuditRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AuditRuleRequiredCodeRule(var message: String) : AuditRuleRule {
    override fun check(item: AuditRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditRuleTrailerIdLengthRule(var maxLength: Int) : AuditRuleRule {
    override fun check(item: AuditRule): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditRuleBatchSizeRangeRule(var min: Int, var max: Int) : AuditRuleRule {
    override fun check(item: AuditRule): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AuditRuleCodeLengthRule(var maxLength: Int) : AuditRuleRule {
    override fun check(item: AuditRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit rule. */
data class AuditRuleValidator(var rules: MutableList<AuditRuleRule>) {
    fun validate(item: AuditRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditRule>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditRuleValidator(): AuditRuleValidator {
    return AuditRuleValidator(
        mutableListOf<AuditRuleRule>(
            AuditRuleCodePrefixRule("Q"),
            AuditRuleRequiredCodeRule("audit rule needs a value"),
            AuditRuleTrailerIdLengthRule(255),
            AuditRuleBatchSizeRangeRule(80, 120),
            AuditRuleCodeLengthRule(64),
        ),
    )
}
