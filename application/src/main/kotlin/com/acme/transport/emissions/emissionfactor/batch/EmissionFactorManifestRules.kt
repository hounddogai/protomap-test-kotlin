package com.acme.transport.emissions.emissionfactor.batch

/** A rule a emission factor manifest must pass. Check returns its problems, or none. */
interface EmissionFactorManifestRule {
    fun check(item: EmissionFactorManifest): MutableList<String>
}

data class EmissionFactorManifestRequiredTitleRule(var message: String) : EmissionFactorManifestRule {
    override fun check(item: EmissionFactorManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorManifestBatchSizeRangeRule(var min: Int, var max: Int) : EmissionFactorManifestRule {
    override fun check(item: EmissionFactorManifest): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class EmissionFactorManifestArchivedGuardRule(var limit: Int) : EmissionFactorManifestRule {
    override fun check(item: EmissionFactorManifest): MutableList<String> {
        if (item.archived && item.createdAt > limit) {
            return mutableListOf<String>("created at ${item.createdAt} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor manifest. */
data class EmissionFactorManifestValidator(var rules: MutableList<EmissionFactorManifestRule>) {
    fun validate(item: EmissionFactorManifest): MutableList<String> {
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

    fun requireValid(item: EmissionFactorManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorManifestValidator(): EmissionFactorManifestValidator {
    return EmissionFactorManifestValidator(
        mutableListOf<EmissionFactorManifestRule>(
            EmissionFactorManifestRequiredTitleRule("emission factor manifest needs an entry"),
            EmissionFactorManifestBatchSizeRangeRule(255, 86400),
            EmissionFactorManifestArchivedGuardRule(3600),
        ),
    )
}
