package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.kit.text.normalize

/** A rule a pending backhaul mapping must pass. Check returns its problems, or none. */
interface PendingBackhaulMappingRule {
    fun check(item: PendingBackhaulMapping): MutableList<String>
}

data class PendingBackhaulMappingCodePrefixRule(var prefix: String) : PendingBackhaulMappingRule {
    override fun check(item: PendingBackhaulMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingBackhaulMappingUpdatedAtRangeRule(var min: Int, var max: Int) : PendingBackhaulMappingRule {
    override fun check(item: PendingBackhaulMapping): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingBackhaulMappingAllowedNameRule(
    var allowed: MutableList<String>,
) : PendingBackhaulMappingRule {
    override fun check(item: PendingBackhaulMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending backhaul mapping. */
data class PendingBackhaulMappingValidator(var rules: MutableList<PendingBackhaulMappingRule>) {
    fun validate(item: PendingBackhaulMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingBackhaulMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingBackhaulMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingBackhaulMappingValidator(): PendingBackhaulMappingValidator {
    return PendingBackhaulMappingValidator(
        mutableListOf<PendingBackhaulMappingRule>(
            PendingBackhaulMappingCodePrefixRule("REF"),
            PendingBackhaulMappingUpdatedAtRangeRule(32, 32),
            PendingBackhaulMappingAllowedNameRule(mutableListOf<String>()),
        ),
    )
}
