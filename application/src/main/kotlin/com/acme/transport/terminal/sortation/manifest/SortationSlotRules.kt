package com.acme.transport.terminal.sortation.manifest

/** A rule a sortation slot must pass. Check returns its problems, or none. */
interface SortationSlotRule {
    fun check(item: SortationSlot): MutableList<String>
}

data class SortationSlotStopCountRangeRule(var min: Int, var max: Int) : SortationSlotRule {
    override fun check(item: SortationSlot): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SortationSlotLabelLengthRule(var maxLength: Int) : SortationSlotRule {
    override fun check(item: SortationSlot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationSlotLoadKgRangeRule(var min: Double, var max: Double) : SortationSlotRule {
    override fun check(item: SortationSlot): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class SortationSlotLocaleLengthRule(var maxLength: Int) : SortationSlotRule {
    override fun check(item: SortationSlot): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationSlotRequiredLocaleRule(var message: String) : SortationSlotRule {
    override fun check(item: SortationSlot): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation slot. */
data class SortationSlotValidator(var rules: MutableList<SortationSlotRule>) {
    fun validate(item: SortationSlot): MutableList<String> {
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

    fun requireValid(item: SortationSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationSlotValidator(): SortationSlotValidator {
    return SortationSlotValidator(
        mutableListOf<SortationSlotRule>(
            SortationSlotStopCountRangeRule(16, 255),
            SortationSlotLabelLengthRule(64),
            SortationSlotLoadKgRangeRule(1.9, 1384.7),
            SortationSlotLocaleLengthRule(120),
            SortationSlotRequiredLocaleRule("sortation slot needs this field"),
        ),
    )
}
