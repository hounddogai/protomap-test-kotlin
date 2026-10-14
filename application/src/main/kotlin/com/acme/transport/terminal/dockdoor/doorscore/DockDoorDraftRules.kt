package com.acme.transport.terminal.dockdoor.doorscore

import com.acme.transport.kit.text.normalize

/** A rule a dock door draft must pass. Check returns its problems, or none. */
interface DockDoorDraftRule {
    fun check(item: DockDoorDraft): MutableList<String>
}

data class DockDoorDraftAllowedNameRule(var allowed: MutableList<String>) : DockDoorDraftRule {
    override fun check(item: DockDoorDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorDraftRequiredPhoneRule(var message: String) : DockDoorDraftRule {
    override fun check(item: DockDoorDraft): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorDraftRequiredNameRule(var message: String) : DockDoorDraftRule {
    override fun check(item: DockDoorDraft): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorDraftStartsAtRangeRule(var min: Int, var max: Int) : DockDoorDraftRule {
    override fun check(item: DockDoorDraft): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorDraftRequiredCodeRule(var message: String) : DockDoorDraftRule {
    override fun check(item: DockDoorDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door draft. */
data class DockDoorDraftValidator(var rules: MutableList<DockDoorDraftRule>) {
    fun validate(item: DockDoorDraft): MutableList<String> {
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

    fun requireValid(item: DockDoorDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorDraftValidator(): DockDoorDraftValidator {
    return DockDoorDraftValidator(
        mutableListOf<DockDoorDraftRule>(
            DockDoorDraftAllowedNameRule(mutableListOf<String>()),
            DockDoorDraftRequiredPhoneRule("dock door draft needs a value"),
            DockDoorDraftRequiredNameRule("dock door draft needs this field"),
            DockDoorDraftStartsAtRangeRule(255, 86400),
            DockDoorDraftRequiredCodeRule("dock door draft needs this field"),
        ),
    )
}
