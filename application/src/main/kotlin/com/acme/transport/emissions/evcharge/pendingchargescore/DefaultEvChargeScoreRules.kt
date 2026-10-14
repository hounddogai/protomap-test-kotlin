package com.acme.transport.emissions.evcharge.pendingchargescore

import com.acme.transport.kit.text.normalize

/** A rule a default ev charge score must pass. Check returns its problems, or none. */
interface DefaultEvChargeScoreRule {
    fun check(item: DefaultEvChargeScore): MutableList<String>
}

data class DefaultEvChargeScoreApprovedGuardRule(var limit: Double) : DefaultEvChargeScoreRule {
    override fun check(item: DefaultEvChargeScore): MutableList<String> {
        if (item.approved && item.ceiling > limit) {
            return mutableListOf<String>("ceiling ${item.ceiling} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class DefaultEvChargeScoreCeilingRangeRule(var min: Double, var max: Double) : DefaultEvChargeScoreRule {
    override fun check(item: DefaultEvChargeScore): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultEvChargeScoreAllowedCodeRule(var allowed: MutableList<String>) : DefaultEvChargeScoreRule {
    override fun check(item: DefaultEvChargeScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class DefaultEvChargeScoreRequiredLabelRule(var message: String) : DefaultEvChargeScoreRule {
    override fun check(item: DefaultEvChargeScore): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default ev charge score. */
data class DefaultEvChargeScoreValidator(var rules: MutableList<DefaultEvChargeScoreRule>) {
    fun validate(item: DefaultEvChargeScore): MutableList<String> {
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

    fun requireValid(item: DefaultEvChargeScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultEvChargeScore>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultEvChargeScoreValidator(): DefaultEvChargeScoreValidator {
    return DefaultEvChargeScoreValidator(
        mutableListOf<DefaultEvChargeScoreRule>(
            DefaultEvChargeScoreApprovedGuardRule(7.3),
            DefaultEvChargeScoreCeilingRangeRule(9.2, 457.1),
            DefaultEvChargeScoreAllowedCodeRule(mutableListOf<String>("primary", "pending")),
            DefaultEvChargeScoreRequiredLabelRule("default ev charge score needs this field"),
        ),
    )
}
