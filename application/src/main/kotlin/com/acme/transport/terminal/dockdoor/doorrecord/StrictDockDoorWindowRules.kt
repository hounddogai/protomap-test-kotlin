package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.text.normalize

/** A rule a strict dock door window must pass. Check returns its problems, or none. */
interface StrictDockDoorWindowRule {
    fun check(item: StrictDockDoorWindow): MutableList<String>
}

data class StrictDockDoorWindowCodeLengthRule(var maxLength: Int) : StrictDockDoorWindowRule {
    override fun check(item: StrictDockDoorWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDockDoorWindowSummaryLengthRule(var maxLength: Int) : StrictDockDoorWindowRule {
    override fun check(item: StrictDockDoorWindow): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDockDoorWindowPinnedGuardRule(var limit: Double) : StrictDockDoorWindowRule {
    override fun check(item: StrictDockDoorWindow): MutableList<String> {
        if (item.pinned && item.fuelLiters > limit) {
            return mutableListOf<String>("fuel liters ${item.fuelLiters} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class StrictDockDoorWindowAllowedCodeRule(var allowed: MutableList<String>) : StrictDockDoorWindowRule {
    override fun check(item: StrictDockDoorWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict dock door window. */
data class StrictDockDoorWindowValidator(var rules: MutableList<StrictDockDoorWindowRule>) {
    fun validate(item: StrictDockDoorWindow): MutableList<String> {
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

    fun requireValid(item: StrictDockDoorWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDockDoorWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDockDoorWindowValidator(): StrictDockDoorWindowValidator {
    return StrictDockDoorWindowValidator(
        mutableListOf<StrictDockDoorWindowRule>(
            StrictDockDoorWindowCodeLengthRule(255),
            StrictDockDoorWindowSummaryLengthRule(32),
            StrictDockDoorWindowPinnedGuardRule(6.7),
            StrictDockDoorWindowAllowedCodeRule(mutableListOf<String>("weighted", "primary")),
        ),
    )
}
