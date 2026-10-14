package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.text.normalize

/** A rule a weighted backhaul audit must pass. Check returns its problems, or none. */
interface WeightedBackhaulAuditRule {
    fun check(item: WeightedBackhaulAudit): MutableList<String>
}

data class WeightedBackhaulAuditRequiredNameRule(var message: String) : WeightedBackhaulAuditRule {
    override fun check(item: WeightedBackhaulAudit): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedBackhaulAuditAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : WeightedBackhaulAuditRule {
    override fun check(item: WeightedBackhaulAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class WeightedBackhaulAuditAmountRangeRule(var min: Double, var max: Double) : WeightedBackhaulAuditRule {
    override fun check(item: WeightedBackhaulAudit): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedBackhaulAuditCodePrefixRule(var prefix: String) : WeightedBackhaulAuditRule {
    override fun check(item: WeightedBackhaulAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedBackhaulAuditRequiredDockRule(var message: String) : WeightedBackhaulAuditRule {
    override fun check(item: WeightedBackhaulAudit): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedBackhaulAuditCodeLengthRule(var maxLength: Int) : WeightedBackhaulAuditRule {
    override fun check(item: WeightedBackhaulAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted backhaul audit. */
data class WeightedBackhaulAuditValidator(var rules: MutableList<WeightedBackhaulAuditRule>) {
    fun validate(item: WeightedBackhaulAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedBackhaulAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedBackhaulAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedBackhaulAuditValidator(): WeightedBackhaulAuditValidator {
    return WeightedBackhaulAuditValidator(
        mutableListOf<WeightedBackhaulAuditRule>(
            WeightedBackhaulAuditRequiredNameRule("weighted backhaul audit needs a value"),
            WeightedBackhaulAuditAllowedExternalRefRule(mutableListOf<String>()),
            WeightedBackhaulAuditAmountRangeRule(8.8, 476.4),
            WeightedBackhaulAuditCodePrefixRule("OPS"),
            WeightedBackhaulAuditRequiredDockRule("weighted backhaul audit needs this field"),
            WeightedBackhaulAuditCodeLengthRule(32),
        ),
    )
}
