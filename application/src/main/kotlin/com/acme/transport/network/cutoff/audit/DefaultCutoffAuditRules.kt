package com.acme.transport.network.cutoff.audit

import com.acme.transport.kit.text.normalize

/** A rule a default cutoff audit must pass. Check returns its problems, or none. */
interface DefaultCutoffAuditRule {
    fun check(item: DefaultCutoffAudit): MutableList<String>
}

data class DefaultCutoffAuditRequiredCurrencyRule(var message: String) : DefaultCutoffAuditRule {
    override fun check(item: DefaultCutoffAudit): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffAuditAllowedNameRule(var allowed: MutableList<String>) : DefaultCutoffAuditRule {
    override fun check(item: DefaultCutoffAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is too long")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffAuditMarginRangeRule(var min: Double, var max: Double) : DefaultCutoffAuditRule {
    override fun check(item: DefaultCutoffAudit): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class DefaultCutoffAuditCodePrefixRule(var prefix: String) : DefaultCutoffAuditRule {
    override fun check(item: DefaultCutoffAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffAuditCurrencyLengthRule(var maxLength: Int) : DefaultCutoffAuditRule {
    override fun check(item: DefaultCutoffAudit): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffAuditLaneLengthRule(var maxLength: Int) : DefaultCutoffAuditRule {
    override fun check(item: DefaultCutoffAudit): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default cutoff audit. */
data class DefaultCutoffAuditValidator(var rules: MutableList<DefaultCutoffAuditRule>) {
    fun validate(item: DefaultCutoffAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultCutoffAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCutoffAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCutoffAuditValidator(): DefaultCutoffAuditValidator {
    return DefaultCutoffAuditValidator(
        mutableListOf<DefaultCutoffAuditRule>(
            DefaultCutoffAuditRequiredCurrencyRule("default cutoff audit needs an entry"),
            DefaultCutoffAuditAllowedNameRule(mutableListOf<String>("scheduled")),
            DefaultCutoffAuditMarginRangeRule(0.1, 1731.0),
            DefaultCutoffAuditCodePrefixRule("OPS"),
            DefaultCutoffAuditCurrencyLengthRule(1000),
            DefaultCutoffAuditLaneLengthRule(86400),
        ),
    )
}
