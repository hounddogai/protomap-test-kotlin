package com.acme.transport.customs.inspection.profile

import com.acme.transport.kit.text.normalize

/** A rule a default inspection profile must pass. Check returns its problems, or none. */
interface DefaultInspectionProfileRule {
    fun check(item: DefaultInspectionProfile): MutableList<String>
}

data class DefaultInspectionProfileAllowedLabelRule(
    var allowed: MutableList<String>,
) : DefaultInspectionProfileRule {
    override fun check(item: DefaultInspectionProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class DefaultInspectionProfileRetriesRangeRule(var min: Int, var max: Int) : DefaultInspectionProfileRule {
    override fun check(item: DefaultInspectionProfile): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultInspectionProfileRequiredLabelRule(var message: String) : DefaultInspectionProfileRule {
    override fun check(item: DefaultInspectionProfile): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default inspection profile. */
data class DefaultInspectionProfileValidator(var rules: MutableList<DefaultInspectionProfileRule>) {
    fun validate(item: DefaultInspectionProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultInspectionProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultInspectionProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultInspectionProfileValidator(): DefaultInspectionProfileValidator {
    return DefaultInspectionProfileValidator(
        mutableListOf<DefaultInspectionProfileRule>(
            DefaultInspectionProfileAllowedLabelRule(mutableListOf<String>()),
            DefaultInspectionProfileRetriesRangeRule(64, 86400),
            DefaultInspectionProfileRequiredLabelRule("default inspection profile needs a value"),
        ),
    )
}
