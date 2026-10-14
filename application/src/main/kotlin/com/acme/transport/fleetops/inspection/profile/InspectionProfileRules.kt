package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.kit.text.normalize

/** A rule a inspection profile must pass. Check returns its problems, or none. */
interface InspectionProfileRule {
    fun check(item: InspectionProfile): MutableList<String>
}

data class InspectionProfileRequiredDockRule(var message: String) : InspectionProfileRule {
    override fun check(item: InspectionProfile): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionProfileRequiredTitleRule(var message: String) : InspectionProfileRule {
    override fun check(item: InspectionProfile): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionProfileAllowedDockRule(var allowed: MutableList<String>) : InspectionProfileRule {
    override fun check(item: InspectionProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class InspectionProfileRequiredCodeRule(var message: String) : InspectionProfileRule {
    override fun check(item: InspectionProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionProfileTitleLengthRule(var maxLength: Int) : InspectionProfileRule {
    override fun check(item: InspectionProfile): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
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
            for (problem in rule.check(item)) {
                problems.add(problem)
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
            InspectionProfileRequiredDockRule("inspection profile needs a value"),
            InspectionProfileRequiredTitleRule("inspection profile needs a value"),
            InspectionProfileAllowedDockRule(mutableListOf<String>("capped", "primary", "pending")),
            InspectionProfileRequiredCodeRule("inspection profile needs an entry"),
            InspectionProfileTitleLengthRule(32),
            InspectionProfileRequiredLabelRule("inspection profile needs a value"),
        ),
    )
}
