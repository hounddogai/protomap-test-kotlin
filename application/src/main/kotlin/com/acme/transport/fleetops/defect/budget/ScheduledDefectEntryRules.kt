package com.acme.transport.fleetops.defect.budget

import com.acme.transport.kit.text.normalize

/** A rule a scheduled defect entry must pass. Check returns its problems, or none. */
interface ScheduledDefectEntryRule {
    fun check(item: ScheduledDefectEntry): MutableList<String>
}

data class ScheduledDefectEntryRequiredCodeRule(var message: String) : ScheduledDefectEntryRule {
    override fun check(item: ScheduledDefectEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectEntryAllowedTitleRule(var allowed: MutableList<String>) : ScheduledDefectEntryRule {
    override fun check(item: ScheduledDefectEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectEntryLabelLengthRule(var maxLength: Int) : ScheduledDefectEntryRule {
    override fun check(item: ScheduledDefectEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectEntryRequiredRegionRule(var message: String) : ScheduledDefectEntryRule {
    override fun check(item: ScheduledDefectEntry): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectEntryRequiredLabelRule(var message: String) : ScheduledDefectEntryRule {
    override fun check(item: ScheduledDefectEntry): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled defect entry. */
data class ScheduledDefectEntryValidator(var rules: MutableList<ScheduledDefectEntryRule>) {
    fun validate(item: ScheduledDefectEntry): MutableList<String> {
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

    fun requireValid(item: ScheduledDefectEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDefectEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDefectEntryValidator(): ScheduledDefectEntryValidator {
    return ScheduledDefectEntryValidator(
        mutableListOf<ScheduledDefectEntryRule>(
            ScheduledDefectEntryRequiredCodeRule("scheduled defect entry needs this field"),
            ScheduledDefectEntryAllowedTitleRule(mutableListOf<String>("regional", "default", "pending")),
            ScheduledDefectEntryLabelLengthRule(3600),
            ScheduledDefectEntryRequiredRegionRule("scheduled defect entry needs an entry"),
            ScheduledDefectEntryRequiredLabelRule("scheduled defect entry needs an entry"),
        ),
    )
}
