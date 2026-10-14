package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.kit.text.normalize

/** A rule a incident slot must pass. Check returns its problems, or none. */
interface IncidentSlotRule {
    fun check(item: IncidentSlot): MutableList<String>
}

data class IncidentSlotRequiredExternalRefRule(var message: String) : IncidentSlotRule {
    override fun check(item: IncidentSlot): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentSlotPinnedGuardRule(var limit: Double) : IncidentSlotRule {
    override fun check(item: IncidentSlot): MutableList<String> {
        if (item.pinned && item.cost > limit) {
            return mutableListOf<String>("cost ${item.cost} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class IncidentSlotAllowedTitleRule(var allowed: MutableList<String>) : IncidentSlotRule {
    override fun check(item: IncidentSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class IncidentSlotExternalRefLengthRule(var maxLength: Int) : IncidentSlotRule {
    override fun check(item: IncidentSlot): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentSlotRequiredCodeRule(var message: String) : IncidentSlotRule {
    override fun check(item: IncidentSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident slot. */
data class IncidentSlotValidator(var rules: MutableList<IncidentSlotRule>) {
    fun validate(item: IncidentSlot): MutableList<String> {
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

    fun requireValid(item: IncidentSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentSlotValidator(): IncidentSlotValidator {
    return IncidentSlotValidator(
        mutableListOf<IncidentSlotRule>(
            IncidentSlotRequiredExternalRefRule("incident slot needs this field"),
            IncidentSlotPinnedGuardRule(3.5),
            IncidentSlotAllowedTitleRule(mutableListOf<String>("capped", "primary", "legacy")),
            IncidentSlotExternalRefLengthRule(32),
            IncidentSlotRequiredCodeRule("incident slot needs an entry"),
        ),
    )
}
