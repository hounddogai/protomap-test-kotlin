package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.kit.text.normalize

/** A rule a emission factor batch must pass. Check returns its problems, or none. */
interface EmissionFactorBatchRule {
    fun check(item: EmissionFactorBatch): MutableList<String>
}

data class EmissionFactorBatchCodePrefixRule(var prefix: String) : EmissionFactorBatchRule {
    override fun check(item: EmissionFactorBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorBatchNotesLengthRule(var maxLength: Int) : EmissionFactorBatchRule {
    override fun check(item: EmissionFactorBatch): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorBatchAllowedNotesRule(var allowed: MutableList<String>) : EmissionFactorBatchRule {
    override fun check(item: EmissionFactorBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor batch. */
data class EmissionFactorBatchValidator(var rules: MutableList<EmissionFactorBatchRule>) {
    fun validate(item: EmissionFactorBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EmissionFactorBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorBatchValidator(): EmissionFactorBatchValidator {
    return EmissionFactorBatchValidator(
        mutableListOf<EmissionFactorBatchRule>(
            EmissionFactorBatchCodePrefixRule("ACM"),
            EmissionFactorBatchNotesLengthRule(16),
            EmissionFactorBatchAllowedNotesRule(mutableListOf<String>("strict")),
        ),
    )
}
