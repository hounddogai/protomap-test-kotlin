package com.acme.transport.network.transittime.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a legacy transit time checkpoint must pass. Check returns its problems, or none. */
interface LegacyTransitTimeCheckpointRule {
    fun check(item: LegacyTransitTimeCheckpoint): MutableList<String>
}

data class LegacyTransitTimeCheckpointAllowedShiftCodeRule(
    var allowed: MutableList<String>,
) : LegacyTransitTimeCheckpointRule {
    override fun check(item: LegacyTransitTimeCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is expired")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTransitTimeCheckpointRequiredLabelRule(var message: String) : LegacyTransitTimeCheckpointRule {
    override fun check(item: LegacyTransitTimeCheckpoint): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyTransitTimeCheckpointRequiredShiftCodeRule(
    var message: String,
) : LegacyTransitTimeCheckpointRule {
    override fun check(item: LegacyTransitTimeCheckpoint): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy transit time checkpoint. */
data class LegacyTransitTimeCheckpointValidator(var rules: MutableList<LegacyTransitTimeCheckpointRule>) {
    fun validate(item: LegacyTransitTimeCheckpoint): MutableList<String> {
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

    fun requireValid(item: LegacyTransitTimeCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTransitTimeCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTransitTimeCheckpointValidator(): LegacyTransitTimeCheckpointValidator {
    return LegacyTransitTimeCheckpointValidator(
        mutableListOf<LegacyTransitTimeCheckpointRule>(
            LegacyTransitTimeCheckpointAllowedShiftCodeRule(mutableListOf<String>()),
            LegacyTransitTimeCheckpointRequiredLabelRule("legacy transit time checkpoint needs a value"),
            LegacyTransitTimeCheckpointRequiredShiftCodeRule("legacy transit time checkpoint needs this field"),
        ),
    )
}
