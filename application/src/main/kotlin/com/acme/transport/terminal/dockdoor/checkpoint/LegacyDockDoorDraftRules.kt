package com.acme.transport.terminal.dockdoor.checkpoint

/** A rule a legacy dock door draft must pass. Check returns its problems, or none. */
interface LegacyDockDoorDraftRule {
    fun check(item: LegacyDockDoorDraft): MutableList<String>
}

data class LegacyDockDoorDraftDescriptionLengthRule(var maxLength: Int) : LegacyDockDoorDraftRule {
    override fun check(item: LegacyDockDoorDraft): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDockDoorDraftRequiredDescriptionRule(var message: String) : LegacyDockDoorDraftRule {
    override fun check(item: LegacyDockDoorDraft): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDockDoorDraftRequiredCodeRule(var message: String) : LegacyDockDoorDraftRule {
    override fun check(item: LegacyDockDoorDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDockDoorDraftLabelLengthRule(var maxLength: Int) : LegacyDockDoorDraftRule {
    override fun check(item: LegacyDockDoorDraft): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDockDoorDraftUpdatedAtRangeRule(var min: Int, var max: Int) : LegacyDockDoorDraftRule {
    override fun check(item: LegacyDockDoorDraft): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class LegacyDockDoorDraftCodeLengthRule(var maxLength: Int) : LegacyDockDoorDraftRule {
    override fun check(item: LegacyDockDoorDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy dock door draft. */
data class LegacyDockDoorDraftValidator(var rules: MutableList<LegacyDockDoorDraftRule>) {
    fun validate(item: LegacyDockDoorDraft): MutableList<String> {
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

    fun requireValid(item: LegacyDockDoorDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDockDoorDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDockDoorDraftValidator(): LegacyDockDoorDraftValidator {
    return LegacyDockDoorDraftValidator(
        mutableListOf<LegacyDockDoorDraftRule>(
            LegacyDockDoorDraftDescriptionLengthRule(16),
            LegacyDockDoorDraftRequiredDescriptionRule("legacy dock door draft needs this field"),
            LegacyDockDoorDraftRequiredCodeRule("legacy dock door draft needs this field"),
            LegacyDockDoorDraftLabelLengthRule(3600),
            LegacyDockDoorDraftUpdatedAtRangeRule(16, 3600),
            LegacyDockDoorDraftCodeLengthRule(80),
        ),
    )
}
