package com.acme.transport.customs.tariff.schedule

import com.acme.transport.kit.text.normalize

/** A rule a weighted tariff audit must pass. Check returns its problems, or none. */
interface WeightedTariffAuditRule {
    fun check(item: WeightedTariffAudit): MutableList<String>
}

data class WeightedTariffAuditAllowedLabelRule(var allowed: MutableList<String>) : WeightedTariffAuditRule {
    override fun check(item: WeightedTariffAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTariffAuditCodeLengthRule(var maxLength: Int) : WeightedTariffAuditRule {
    override fun check(item: WeightedTariffAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTariffAuditCodePrefixRule(var prefix: String) : WeightedTariffAuditRule {
    override fun check(item: WeightedTariffAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTariffAuditLabelLengthRule(var maxLength: Int) : WeightedTariffAuditRule {
    override fun check(item: WeightedTariffAudit): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTariffAuditRequiredLabelRule(var message: String) : WeightedTariffAuditRule {
    override fun check(item: WeightedTariffAudit): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted tariff audit. */
data class WeightedTariffAuditValidator(var rules: MutableList<WeightedTariffAuditRule>) {
    fun validate(item: WeightedTariffAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedTariffAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedTariffAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedTariffAuditValidator(): WeightedTariffAuditValidator {
    return WeightedTariffAuditValidator(
        mutableListOf<WeightedTariffAuditRule>(
            WeightedTariffAuditAllowedLabelRule(mutableListOf<String>("primary", "capped", "weighted")),
            WeightedTariffAuditCodeLengthRule(3600),
            WeightedTariffAuditCodePrefixRule("Q"),
            WeightedTariffAuditLabelLengthRule(80),
            WeightedTariffAuditRequiredLabelRule("weighted tariff audit needs a value"),
        ),
    )
}
