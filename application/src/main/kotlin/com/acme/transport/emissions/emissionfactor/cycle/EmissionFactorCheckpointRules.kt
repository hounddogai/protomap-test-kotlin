package com.acme.transport.emissions.emissionfactor.cycle

import com.acme.transport.kit.text.normalize

/** A rule a emission factor checkpoint must pass. Check returns its problems, or none. */
interface EmissionFactorCheckpointRule {
    fun check(item: EmissionFactorCheckpoint): MutableList<String>
}

data class EmissionFactorCheckpointRequiredTitleRule(var message: String) : EmissionFactorCheckpointRule {
    override fun check(item: EmissionFactorCheckpoint): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorCheckpointAllowedTitleRule(
    var allowed: MutableList<String>,
) : EmissionFactorCheckpointRule {
    override fun check(item: EmissionFactorCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorCheckpointTitleLengthRule(var maxLength: Int) : EmissionFactorCheckpointRule {
    override fun check(item: EmissionFactorCheckpoint): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorCheckpointRequiredReviewerRule(var message: String) : EmissionFactorCheckpointRule {
    override fun check(item: EmissionFactorCheckpoint): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorCheckpointLabelLengthRule(var maxLength: Int) : EmissionFactorCheckpointRule {
    override fun check(item: EmissionFactorCheckpoint): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor checkpoint. */
data class EmissionFactorCheckpointValidator(var rules: MutableList<EmissionFactorCheckpointRule>) {
    fun validate(item: EmissionFactorCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EmissionFactorCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorCheckpointValidator(): EmissionFactorCheckpointValidator {
    return EmissionFactorCheckpointValidator(
        mutableListOf<EmissionFactorCheckpointRule>(
            EmissionFactorCheckpointRequiredTitleRule("emission factor checkpoint needs this field"),
            EmissionFactorCheckpointAllowedTitleRule(mutableListOf<String>("capped", "legacy")),
            EmissionFactorCheckpointTitleLengthRule(255),
            EmissionFactorCheckpointRequiredReviewerRule("emission factor checkpoint needs an entry"),
            EmissionFactorCheckpointLabelLengthRule(1000),
        ),
    )
}
