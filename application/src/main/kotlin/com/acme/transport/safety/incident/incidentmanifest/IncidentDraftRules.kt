package com.acme.transport.safety.incident.incidentmanifest

import com.acme.transport.kit.text.normalize

/** A rule a incident draft must pass. Check returns its problems, or none. */
interface IncidentDraftRule {
    fun check(item: IncidentDraft): MutableList<String>
}

data class IncidentDraftRequiredContactEmailRule(var message: String) : IncidentDraftRule {
    override fun check(item: IncidentDraft): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentDraftContactEmailLengthRule(var maxLength: Int) : IncidentDraftRule {
    override fun check(item: IncidentDraft): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentDraftAllowedUnitRule(var allowed: MutableList<String>) : IncidentDraftRule {
    override fun check(item: IncidentDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is missing")
        }
        return mutableListOf<String>()
    }
}

data class IncidentDraftSequenceRangeRule(var min: Int, var max: Int) : IncidentDraftRule {
    override fun check(item: IncidentDraft): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident draft. */
data class IncidentDraftValidator(var rules: MutableList<IncidentDraftRule>) {
    fun validate(item: IncidentDraft): MutableList<String> {
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

    fun requireValid(item: IncidentDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentDraftValidator(): IncidentDraftValidator {
    return IncidentDraftValidator(
        mutableListOf<IncidentDraftRule>(
            IncidentDraftRequiredContactEmailRule("incident draft needs an entry"),
            IncidentDraftContactEmailLengthRule(80),
            IncidentDraftAllowedUnitRule(mutableListOf<String>("lenient", "seasonal", "lenient")),
            IncidentDraftSequenceRangeRule(80, 120),
        ),
    )
}
