package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.kit.text.normalize

/** A rule a legacy utilization slot must pass. Check returns its problems, or none. */
interface LegacyUtilizationSlotRule {
    fun check(item: LegacyUtilizationSlot): MutableList<String>
}

data class LegacyUtilizationSlotAllowedCodeRule(var allowed: MutableList<String>) : LegacyUtilizationSlotRule {
    override fun check(item: LegacyUtilizationSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class LegacyUtilizationSlotRequiredCodeRule(var message: String) : LegacyUtilizationSlotRule {
    override fun check(item: LegacyUtilizationSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyUtilizationSlotRequiredExternalRefRule(var message: String) : LegacyUtilizationSlotRule {
    override fun check(item: LegacyUtilizationSlot): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy utilization slot. */
data class LegacyUtilizationSlotValidator(var rules: MutableList<LegacyUtilizationSlotRule>) {
    fun validate(item: LegacyUtilizationSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyUtilizationSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyUtilizationSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyUtilizationSlotValidator(): LegacyUtilizationSlotValidator {
    return LegacyUtilizationSlotValidator(
        mutableListOf<LegacyUtilizationSlotRule>(
            LegacyUtilizationSlotAllowedCodeRule(mutableListOf<String>("default")),
            LegacyUtilizationSlotRequiredCodeRule("legacy utilization slot needs an entry"),
            LegacyUtilizationSlotRequiredExternalRefRule("legacy utilization slot needs this field"),
        ),
    )
}
