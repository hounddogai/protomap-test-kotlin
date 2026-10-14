package com.acme.transport.terminal.dockdoor.slot

/** A rule a fallback dock door slot must pass. Check returns its problems, or none. */
interface FallbackDockDoorSlotRule {
    fun check(item: FallbackDockDoorSlot): MutableList<String>
}

data class FallbackDockDoorSlotRequiredLocaleRule(var message: String) : FallbackDockDoorSlotRule {
    override fun check(item: FallbackDockDoorSlot): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDockDoorSlotCategoryLengthRule(var maxLength: Int) : FallbackDockDoorSlotRule {
    override fun check(item: FallbackDockDoorSlot): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDockDoorSlotVersionRangeRule(var min: Int, var max: Int) : FallbackDockDoorSlotRule {
    override fun check(item: FallbackDockDoorSlot): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback dock door slot. */
data class FallbackDockDoorSlotValidator(var rules: MutableList<FallbackDockDoorSlotRule>) {
    fun validate(item: FallbackDockDoorSlot): MutableList<String> {
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

    fun requireValid(item: FallbackDockDoorSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDockDoorSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDockDoorSlotValidator(): FallbackDockDoorSlotValidator {
    return FallbackDockDoorSlotValidator(
        mutableListOf<FallbackDockDoorSlotRule>(
            FallbackDockDoorSlotRequiredLocaleRule("fallback dock door slot needs an entry"),
            FallbackDockDoorSlotCategoryLengthRule(64),
            FallbackDockDoorSlotVersionRangeRule(64, 1000),
        ),
    )
}
