package com.acme.transport.customs.inspection.metric

import com.acme.transport.kit.text.normalize

/** A rule a inspection profile must pass. Check returns its problems, or none. */
interface InspectionProfileRule {
    fun check(item: InspectionProfile): MutableList<String>
}

data class InspectionProfileAllowedCodeRule(var allowed: MutableList<String>) : InspectionProfileRule {
    override fun check(item: InspectionProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class InspectionProfileExpiresAtRangeRule(var min: Int, var max: Int) : InspectionProfileRule {
    override fun check(item: InspectionProfile): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionProfileRequiredLabelRule(var message: String) : InspectionProfileRule {
    override fun check(item: InspectionProfile): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection profile. */
data class InspectionProfileValidator(var rules: MutableList<InspectionProfileRule>) {
    fun validate(item: InspectionProfile): MutableList<String> {
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

    fun requireValid(item: InspectionProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionProfileValidator(): InspectionProfileValidator {
    return InspectionProfileValidator(
        mutableListOf<InspectionProfileRule>(
            InspectionProfileAllowedCodeRule(mutableListOf<String>("regional", "global")),
            InspectionProfileExpiresAtRangeRule(32, 80),
            InspectionProfileRequiredLabelRule("inspection profile needs a value"),
        ),
    )
}
