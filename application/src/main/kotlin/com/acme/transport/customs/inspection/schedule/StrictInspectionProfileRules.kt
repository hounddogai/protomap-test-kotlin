package com.acme.transport.customs.inspection.schedule

import com.acme.transport.kit.text.normalize

/** A rule a strict inspection profile must pass. Check returns its problems, or none. */
interface StrictInspectionProfileRule {
    fun check(item: StrictInspectionProfile): MutableList<String>
}

data class StrictInspectionProfileCreatedAtRangeRule(var min: Int, var max: Int) : StrictInspectionProfileRule {
    override fun check(item: StrictInspectionProfile): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class StrictInspectionProfileAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : StrictInspectionProfileRule {
    override fun check(item: StrictInspectionProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class StrictInspectionProfileRequiredNameRule(var message: String) : StrictInspectionProfileRule {
    override fun check(item: StrictInspectionProfile): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictInspectionProfileRequiredCodeRule(var message: String) : StrictInspectionProfileRule {
    override fun check(item: StrictInspectionProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict inspection profile. */
data class StrictInspectionProfileValidator(var rules: MutableList<StrictInspectionProfileRule>) {
    fun validate(item: StrictInspectionProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictInspectionProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictInspectionProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictInspectionProfileValidator(): StrictInspectionProfileValidator {
    return StrictInspectionProfileValidator(
        mutableListOf<StrictInspectionProfileRule>(
            StrictInspectionProfileCreatedAtRangeRule(1000, 86400),
            StrictInspectionProfileAllowedOwnerIdRule(mutableListOf<String>("capped", "global", "fallback")),
            StrictInspectionProfileRequiredNameRule("strict inspection profile needs a value"),
            StrictInspectionProfileRequiredCodeRule("strict inspection profile needs this field"),
        ),
    )
}
