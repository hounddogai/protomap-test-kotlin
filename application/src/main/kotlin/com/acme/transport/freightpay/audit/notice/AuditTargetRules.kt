package com.acme.transport.freightpay.audit.notice

import com.acme.transport.kit.text.normalize

/** A rule a audit target must pass. Check returns its problems, or none. */
interface AuditTargetRule {
    fun check(item: AuditTarget): MutableList<String>
}

data class AuditTargetCodeLengthRule(var maxLength: Int) : AuditTargetRule {
    override fun check(item: AuditTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditTargetAllowedSourceRule(var allowed: MutableList<String>) : AuditTargetRule {
    override fun check(item: AuditTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class AuditTargetBatchSizeRangeRule(var min: Int, var max: Int) : AuditTargetRule {
    override fun check(item: AuditTarget): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AuditTargetCodePrefixRule(var prefix: String) : AuditTargetRule {
    override fun check(item: AuditTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AuditTargetRetriesRangeRule(var min: Int, var max: Int) : AuditTargetRule {
    override fun check(item: AuditTarget): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit target. */
data class AuditTargetValidator(var rules: MutableList<AuditTargetRule>) {
    fun validate(item: AuditTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditTargetValidator(): AuditTargetValidator {
    return AuditTargetValidator(
        mutableListOf<AuditTargetRule>(
            AuditTargetCodeLengthRule(255),
            AuditTargetAllowedSourceRule(mutableListOf<String>("capped")),
            AuditTargetBatchSizeRangeRule(32, 1000),
            AuditTargetCodePrefixRule("OPS"),
            AuditTargetRetriesRangeRule(16, 86400),
        ),
    )
}
