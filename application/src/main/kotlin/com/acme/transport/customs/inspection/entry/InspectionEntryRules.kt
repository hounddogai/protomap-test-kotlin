package com.acme.transport.customs.inspection.entry

import com.acme.transport.kit.text.normalize

/** A rule a inspection entry must pass. Check returns its problems, or none. */
interface InspectionEntryRule {
    fun check(item: InspectionEntry): MutableList<String>
}

data class InspectionEntryEtaMinutesRangeRule(var min: Int, var max: Int) : InspectionEntryRule {
    override fun check(item: InspectionEntry): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class InspectionEntryRequiredTitleRule(var message: String) : InspectionEntryRule {
    override fun check(item: InspectionEntry): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionEntryAllowedContactEmailRule(var allowed: MutableList<String>) : InspectionEntryRule {
    override fun check(item: InspectionEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection entry. */
data class InspectionEntryValidator(var rules: MutableList<InspectionEntryRule>) {
    fun validate(item: InspectionEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionEntryValidator(): InspectionEntryValidator {
    return InspectionEntryValidator(
        mutableListOf<InspectionEntryRule>(
            InspectionEntryEtaMinutesRangeRule(64, 1000),
            InspectionEntryRequiredTitleRule("inspection entry needs this field"),
            InspectionEntryAllowedContactEmailRule(mutableListOf<String>("global")),
        ),
    )
}
