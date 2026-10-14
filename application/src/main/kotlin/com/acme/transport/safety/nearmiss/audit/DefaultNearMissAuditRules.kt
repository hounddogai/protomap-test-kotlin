package com.acme.transport.safety.nearmiss.audit

import com.acme.transport.kit.text.normalize

/** A rule a default near miss audit must pass. Check returns its problems, or none. */
interface DefaultNearMissAuditRule {
    fun check(item: DefaultNearMissAudit): MutableList<String>
}

data class DefaultNearMissAuditRequiredCodeRule(var message: String) : DefaultNearMissAuditRule {
    override fun check(item: DefaultNearMissAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultNearMissAuditMinimumRangeRule(var min: Double, var max: Double) : DefaultNearMissAuditRule {
    override fun check(item: DefaultNearMissAudit): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class DefaultNearMissAuditCodePrefixRule(var prefix: String) : DefaultNearMissAuditRule {
    override fun check(item: DefaultNearMissAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultNearMissAuditTitleLengthRule(var maxLength: Int) : DefaultNearMissAuditRule {
    override fun check(item: DefaultNearMissAudit): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultNearMissAuditAllowedCurrencyRule(
    var allowed: MutableList<String>,
) : DefaultNearMissAuditRule {
    override fun check(item: DefaultNearMissAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.currency))) {
            return mutableListOf<String>("currency ${item.currency} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default near miss audit. */
data class DefaultNearMissAuditValidator(var rules: MutableList<DefaultNearMissAuditRule>) {
    fun validate(item: DefaultNearMissAudit): MutableList<String> {
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

    fun requireValid(item: DefaultNearMissAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultNearMissAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultNearMissAuditValidator(): DefaultNearMissAuditValidator {
    return DefaultNearMissAuditValidator(
        mutableListOf<DefaultNearMissAuditRule>(
            DefaultNearMissAuditRequiredCodeRule("default near miss audit needs this field"),
            DefaultNearMissAuditMinimumRangeRule(0.4, 3737.0),
            DefaultNearMissAuditCodePrefixRule("Q"),
            DefaultNearMissAuditTitleLengthRule(86400),
            DefaultNearMissAuditAllowedCurrencyRule(mutableListOf<String>("default")),
        ),
    )
}
