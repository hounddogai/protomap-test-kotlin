package com.acme.transport.emissions.emissionfactor.profile

import com.acme.transport.kit.text.normalize

/** A rule a batched emission factor manifest must pass. Check returns its problems, or none. */
interface BatchedEmissionFactorManifestRule {
    fun check(item: BatchedEmissionFactorManifest): MutableList<String>
}

data class BatchedEmissionFactorManifestDepotLengthRule(
    var maxLength: Int,
) : BatchedEmissionFactorManifestRule {
    override fun check(item: BatchedEmissionFactorManifest): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedEmissionFactorManifestMinimumRangeRule(
    var min: Double,
    var max: Double,
) : BatchedEmissionFactorManifestRule {
    override fun check(item: BatchedEmissionFactorManifest): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedEmissionFactorManifestAllowedCodeRule(
    var allowed: MutableList<String>,
) : BatchedEmissionFactorManifestRule {
    override fun check(item: BatchedEmissionFactorManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class BatchedEmissionFactorManifestRequiredCodeRule(
    var message: String,
) : BatchedEmissionFactorManifestRule {
    override fun check(item: BatchedEmissionFactorManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched emission factor manifest. */
data class BatchedEmissionFactorManifestValidator(var rules: MutableList<BatchedEmissionFactorManifestRule>) {
    fun validate(item: BatchedEmissionFactorManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedEmissionFactorManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedEmissionFactorManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedEmissionFactorManifestValidator(): BatchedEmissionFactorManifestValidator {
    return BatchedEmissionFactorManifestValidator(
        mutableListOf<BatchedEmissionFactorManifestRule>(
            BatchedEmissionFactorManifestDepotLengthRule(64),
            BatchedEmissionFactorManifestMinimumRangeRule(9.2, 921.6),
            BatchedEmissionFactorManifestAllowedCodeRule(mutableListOf<String>()),
            BatchedEmissionFactorManifestRequiredCodeRule("batched emission factor manifest needs an entry"),
        ),
    )
}
