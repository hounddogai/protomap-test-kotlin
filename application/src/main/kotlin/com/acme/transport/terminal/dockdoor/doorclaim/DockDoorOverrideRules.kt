package com.acme.transport.terminal.dockdoor.doorclaim

import com.acme.transport.kit.text.normalize

/** A rule a dock door override must pass. Check returns its problems, or none. */
interface DockDoorOverrideRule {
    fun check(item: DockDoorOverride): MutableList<String>
}

data class DockDoorOverrideRequiredCodeRule(var message: String) : DockDoorOverrideRule {
    override fun check(item: DockDoorOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorOverrideAllowedTrailerIdRule(var allowed: MutableList<String>) : DockDoorOverrideRule {
    override fun check(item: DockDoorOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is expired")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorOverrideLoadKgRangeRule(var min: Double, var max: Double) : DockDoorOverrideRule {
    override fun check(item: DockDoorOverride): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door override. */
data class DockDoorOverrideValidator(var rules: MutableList<DockDoorOverrideRule>) {
    fun validate(item: DockDoorOverride): MutableList<String> {
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

    fun requireValid(item: DockDoorOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorOverrideValidator(): DockDoorOverrideValidator {
    return DockDoorOverrideValidator(
        mutableListOf<DockDoorOverrideRule>(
            DockDoorOverrideRequiredCodeRule("dock door override needs an entry"),
            DockDoorOverrideAllowedTrailerIdRule(mutableListOf<String>()),
            DockDoorOverrideLoadKgRangeRule(1.4, 71.0),
        ),
    )
}
