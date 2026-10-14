package com.acme.transport.terminal.sortation.window

/** A rule a rolling sortation window must pass. Check returns its problems, or none. */
interface RollingSortationWindowRule {
    fun check(item: RollingSortationWindow): MutableList<String>
}

data class RollingSortationWindowStopCountRangeRule(var min: Int, var max: Int) : RollingSortationWindowRule {
    override fun check(item: RollingSortationWindow): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingSortationWindowRequiredNameRule(var message: String) : RollingSortationWindowRule {
    override fun check(item: RollingSortationWindow): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingSortationWindowRequiredDockRule(var message: String) : RollingSortationWindowRule {
    override fun check(item: RollingSortationWindow): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling sortation window. */
data class RollingSortationWindowValidator(var rules: MutableList<RollingSortationWindowRule>) {
    fun validate(item: RollingSortationWindow): MutableList<String> {
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

    fun requireValid(item: RollingSortationWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingSortationWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingSortationWindowValidator(): RollingSortationWindowValidator {
    return RollingSortationWindowValidator(
        mutableListOf<RollingSortationWindowRule>(
            RollingSortationWindowStopCountRangeRule(16, 1000),
            RollingSortationWindowRequiredNameRule("rolling sortation window needs a value"),
            RollingSortationWindowRequiredDockRule("rolling sortation window needs a value"),
        ),
    )
}
