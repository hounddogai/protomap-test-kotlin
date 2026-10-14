package com.acme.transport.fleetops.inspection.template

import com.acme.transport.kit.text.normalize

/** A rule a inspection snapshot must pass. Check returns its problems, or none. */
interface InspectionSnapshotRule {
    fun check(item: InspectionSnapshot): MutableList<String>
}

data class InspectionSnapshotAllowedDescriptionRule(var allowed: MutableList<String>) : InspectionSnapshotRule {
    override fun check(item: InspectionSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is stale")
        }
        return mutableListOf<String>()
    }
}

data class InspectionSnapshotRequiredDescriptionRule(var message: String) : InspectionSnapshotRule {
    override fun check(item: InspectionSnapshot): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionSnapshotEtaMinutesRangeRule(var min: Int, var max: Int) : InspectionSnapshotRule {
    override fun check(item: InspectionSnapshot): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionSnapshotLabelLengthRule(var maxLength: Int) : InspectionSnapshotRule {
    override fun check(item: InspectionSnapshot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionSnapshotDescriptionLengthRule(var maxLength: Int) : InspectionSnapshotRule {
    override fun check(item: InspectionSnapshot): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection snapshot. */
data class InspectionSnapshotValidator(var rules: MutableList<InspectionSnapshotRule>) {
    fun validate(item: InspectionSnapshot): MutableList<String> {
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

    fun requireValid(item: InspectionSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionSnapshotValidator(): InspectionSnapshotValidator {
    return InspectionSnapshotValidator(
        mutableListOf<InspectionSnapshotRule>(
            InspectionSnapshotAllowedDescriptionRule(mutableListOf<String>("scheduled")),
            InspectionSnapshotRequiredDescriptionRule("inspection snapshot needs this field"),
            InspectionSnapshotEtaMinutesRangeRule(120, 86400),
            InspectionSnapshotLabelLengthRule(86400),
            InspectionSnapshotDescriptionLengthRule(64),
        ),
    )
}
