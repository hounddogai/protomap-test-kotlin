package com.acme.transport.terminal.dockdoor.window

import com.acme.transport.kit.text.normalize

/** A rule a dock door window must pass. Check returns its problems, or none. */
interface DockDoorWindowRule {
    fun check(item: DockDoorWindow): MutableList<String>
}

data class DockDoorWindowReviewerLengthRule(var maxLength: Int) : DockDoorWindowRule {
    override fun check(item: DockDoorWindow): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorWindowCeilingRangeRule(var min: Double, var max: Double) : DockDoorWindowRule {
    override fun check(item: DockDoorWindow): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class DockDoorWindowRequiredCodeRule(var message: String) : DockDoorWindowRule {
    override fun check(item: DockDoorWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorWindowLabelLengthRule(var maxLength: Int) : DockDoorWindowRule {
    override fun check(item: DockDoorWindow): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorWindowAllowedDockRule(var allowed: MutableList<String>) : DockDoorWindowRule {
    override fun check(item: DockDoorWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorWindowRetriesRangeRule(var min: Int, var max: Int) : DockDoorWindowRule {
    override fun check(item: DockDoorWindow): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a dock door window. */
data class DockDoorWindowValidator(var rules: MutableList<DockDoorWindowRule>) {
    fun validate(item: DockDoorWindow): MutableList<String> {
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

    fun requireValid(item: DockDoorWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorWindowValidator(): DockDoorWindowValidator {
    return DockDoorWindowValidator(
        mutableListOf<DockDoorWindowRule>(
            DockDoorWindowReviewerLengthRule(1000),
            DockDoorWindowCeilingRangeRule(3.5, 3518.2),
            DockDoorWindowRequiredCodeRule("dock door window needs an entry"),
            DockDoorWindowLabelLengthRule(80),
            DockDoorWindowAllowedDockRule(mutableListOf<String>("lenient", "pending")),
            DockDoorWindowRetriesRangeRule(16, 120),
        ),
    )
}
