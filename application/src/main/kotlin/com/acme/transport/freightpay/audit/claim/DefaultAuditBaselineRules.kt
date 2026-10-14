package com.acme.transport.freightpay.audit.claim

import com.acme.transport.kit.text.normalize

/** A rule a default audit baseline must pass. Check returns its problems, or none. */
interface DefaultAuditBaselineRule {
    fun check(item: DefaultAuditBaseline): MutableList<String>
}

data class DefaultAuditBaselineRequiredTitleRule(var message: String) : DefaultAuditBaselineRule {
    override fun check(item: DefaultAuditBaseline): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultAuditBaselineAllowedLabelRule(var allowed: MutableList<String>) : DefaultAuditBaselineRule {
    override fun check(item: DefaultAuditBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAuditBaselineRequiredLabelRule(var message: String) : DefaultAuditBaselineRule {
    override fun check(item: DefaultAuditBaseline): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultAuditBaselineRequiredCodeRule(var message: String) : DefaultAuditBaselineRule {
    override fun check(item: DefaultAuditBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultAuditBaselineWeightRangeRule(var min: Double, var max: Double) : DefaultAuditBaselineRule {
    override fun check(item: DefaultAuditBaseline): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAuditBaselineCodePrefixRule(var prefix: String) : DefaultAuditBaselineRule {
    override fun check(item: DefaultAuditBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default audit baseline. */
data class DefaultAuditBaselineValidator(var rules: MutableList<DefaultAuditBaselineRule>) {
    fun validate(item: DefaultAuditBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultAuditBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultAuditBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultAuditBaselineValidator(): DefaultAuditBaselineValidator {
    return DefaultAuditBaselineValidator(
        mutableListOf<DefaultAuditBaselineRule>(
            DefaultAuditBaselineRequiredTitleRule("default audit baseline needs an entry"),
            DefaultAuditBaselineAllowedLabelRule(mutableListOf<String>("fallback", "seasonal", "batched")),
            DefaultAuditBaselineRequiredLabelRule("default audit baseline needs this field"),
            DefaultAuditBaselineRequiredCodeRule("default audit baseline needs this field"),
            DefaultAuditBaselineWeightRangeRule(9.7, 3245.6),
            DefaultAuditBaselineCodePrefixRule("OPS"),
        ),
    )
}
