package com.acme.transport.lastmile.redelivery.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a redelivery mapping must pass. Check returns its problems, or none. */
interface RedeliveryMappingRule {
    fun check(item: RedeliveryMapping): MutableList<String>
}

data class RedeliveryMappingCodePrefixRule(var prefix: String) : RedeliveryMappingRule {
    override fun check(item: RedeliveryMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryMappingAllowedUnitRule(var allowed: MutableList<String>) : RedeliveryMappingRule {
    override fun check(item: RedeliveryMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is missing")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryMappingNotesLengthRule(var maxLength: Int) : RedeliveryMappingRule {
    override fun check(item: RedeliveryMapping): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery mapping. */
data class RedeliveryMappingValidator(var rules: MutableList<RedeliveryMappingRule>) {
    fun validate(item: RedeliveryMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RedeliveryMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryMappingValidator(): RedeliveryMappingValidator {
    return RedeliveryMappingValidator(
        mutableListOf<RedeliveryMappingRule>(
            RedeliveryMappingCodePrefixRule("REF"),
            RedeliveryMappingAllowedUnitRule(mutableListOf<String>()),
            RedeliveryMappingNotesLengthRule(16),
        ),
    )
}
