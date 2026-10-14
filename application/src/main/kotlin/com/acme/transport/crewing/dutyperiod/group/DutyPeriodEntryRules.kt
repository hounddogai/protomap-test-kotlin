package com.acme.transport.crewing.dutyperiod.group

import com.acme.transport.kit.text.normalize

/** A rule a duty period entry must pass. Check returns its problems, or none. */
interface DutyPeriodEntryRule {
    fun check(item: DutyPeriodEntry): MutableList<String>
}

data class DutyPeriodEntryRequiredCodeRule(var message: String) : DutyPeriodEntryRule {
    override fun check(item: DutyPeriodEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodEntryRequiredSummaryRule(var message: String) : DutyPeriodEntryRule {
    override fun check(item: DutyPeriodEntry): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodEntryCodeLengthRule(var maxLength: Int) : DutyPeriodEntryRule {
    override fun check(item: DutyPeriodEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodEntryAllowedSummaryRule(var allowed: MutableList<String>) : DutyPeriodEntryRule {
    override fun check(item: DutyPeriodEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is expired")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodEntryLabelLengthRule(var maxLength: Int) : DutyPeriodEntryRule {
    override fun check(item: DutyPeriodEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period entry. */
data class DutyPeriodEntryValidator(var rules: MutableList<DutyPeriodEntryRule>) {
    fun validate(item: DutyPeriodEntry): MutableList<String> {
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

    fun requireValid(item: DutyPeriodEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodEntryValidator(): DutyPeriodEntryValidator {
    return DutyPeriodEntryValidator(
        mutableListOf<DutyPeriodEntryRule>(
            DutyPeriodEntryRequiredCodeRule("duty period entry needs a value"),
            DutyPeriodEntryRequiredSummaryRule("duty period entry needs a value"),
            DutyPeriodEntryCodeLengthRule(32),
            DutyPeriodEntryAllowedSummaryRule(mutableListOf<String>("regional")),
            DutyPeriodEntryLabelLengthRule(16),
        ),
    )
}
