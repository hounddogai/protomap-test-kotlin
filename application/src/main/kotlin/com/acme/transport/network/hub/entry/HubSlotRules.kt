package com.acme.transport.network.hub.entry

import com.acme.transport.kit.text.normalize

/** A rule a hub slot must pass. Check returns its problems, or none. */
interface HubSlotRule {
    fun check(item: HubSlot): MutableList<String>
}

data class HubSlotNameLengthRule(var maxLength: Int) : HubSlotRule {
    override fun check(item: HubSlot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubSlotAllowedCodeRule(var allowed: MutableList<String>) : HubSlotRule {
    override fun check(item: HubSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class HubSlotRequiredRegionRule(var message: String) : HubSlotRule {
    override fun check(item: HubSlot): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub slot. */
data class HubSlotValidator(var rules: MutableList<HubSlotRule>) {
    fun validate(item: HubSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HubSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultHubSlotValidator(): HubSlotValidator {
    return HubSlotValidator(
        mutableListOf<HubSlotRule>(
            HubSlotNameLengthRule(80),
            HubSlotAllowedCodeRule(mutableListOf<String>()),
            HubSlotRequiredRegionRule("hub slot needs an entry"),
        ),
    )
}
