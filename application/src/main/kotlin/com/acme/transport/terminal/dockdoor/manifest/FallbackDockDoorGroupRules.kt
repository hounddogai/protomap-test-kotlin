package com.acme.transport.terminal.dockdoor.manifest

/** A rule a fallback dock door group must pass. Check returns its problems, or none. */
interface FallbackDockDoorGroupRule {
    fun check(item: FallbackDockDoorGroup): MutableList<String>
}

data class FallbackDockDoorGroupRequiredTitleRule(var message: String) : FallbackDockDoorGroupRule {
    override fun check(item: FallbackDockDoorGroup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDockDoorGroupTitleLengthRule(var maxLength: Int) : FallbackDockDoorGroupRule {
    override fun check(item: FallbackDockDoorGroup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDockDoorGroupUnitLengthRule(var maxLength: Int) : FallbackDockDoorGroupRule {
    override fun check(item: FallbackDockDoorGroup): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDockDoorGroupRetriesRangeRule(var min: Int, var max: Int) : FallbackDockDoorGroupRule {
    override fun check(item: FallbackDockDoorGroup): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback dock door group. */
data class FallbackDockDoorGroupValidator(var rules: MutableList<FallbackDockDoorGroupRule>) {
    fun validate(item: FallbackDockDoorGroup): MutableList<String> {
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

    fun requireValid(item: FallbackDockDoorGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDockDoorGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDockDoorGroupValidator(): FallbackDockDoorGroupValidator {
    return FallbackDockDoorGroupValidator(
        mutableListOf<FallbackDockDoorGroupRule>(
            FallbackDockDoorGroupRequiredTitleRule("fallback dock door group needs a value"),
            FallbackDockDoorGroupTitleLengthRule(86400),
            FallbackDockDoorGroupUnitLengthRule(80),
            FallbackDockDoorGroupRetriesRangeRule(64, 120),
        ),
    )
}
