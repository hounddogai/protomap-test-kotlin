package com.acme.transport.safety.incident.batch

import com.acme.transport.kit.text.normalize

/** A rule a manual incident segment must pass. Check returns its problems, or none. */
interface ManualIncidentSegmentRule {
    fun check(item: ManualIncidentSegment): MutableList<String>
}

data class ManualIncidentSegmentAllowedDescriptionRule(
    var allowed: MutableList<String>,
) : ManualIncidentSegmentRule {
    override fun check(item: ManualIncidentSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is stale")
        }
        return mutableListOf<String>()
    }
}

data class ManualIncidentSegmentBudgetRangeRule(var min: Double, var max: Double) : ManualIncidentSegmentRule {
    override fun check(item: ManualIncidentSegment): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class ManualIncidentSegmentNotesLengthRule(var maxLength: Int) : ManualIncidentSegmentRule {
    override fun check(item: ManualIncidentSegment): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualIncidentSegmentRequiredCodeRule(var message: String) : ManualIncidentSegmentRule {
    override fun check(item: ManualIncidentSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualIncidentSegmentSegmentLengthRule(var maxLength: Int) : ManualIncidentSegmentRule {
    override fun check(item: ManualIncidentSegment): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual incident segment. */
data class ManualIncidentSegmentValidator(var rules: MutableList<ManualIncidentSegmentRule>) {
    fun validate(item: ManualIncidentSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualIncidentSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualIncidentSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultManualIncidentSegmentValidator(): ManualIncidentSegmentValidator {
    return ManualIncidentSegmentValidator(
        mutableListOf<ManualIncidentSegmentRule>(
            ManualIncidentSegmentAllowedDescriptionRule(mutableListOf<String>("pending")),
            ManualIncidentSegmentBudgetRangeRule(2.3, 3278.7),
            ManualIncidentSegmentNotesLengthRule(255),
            ManualIncidentSegmentRequiredCodeRule("manual incident segment needs this field"),
            ManualIncidentSegmentSegmentLengthRule(3600),
        ),
    )
}
