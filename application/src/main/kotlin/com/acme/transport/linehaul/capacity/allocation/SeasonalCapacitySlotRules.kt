package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.text.normalize

/** A rule a seasonal capacity slot must pass. Check returns its problems, or none. */
interface SeasonalCapacitySlotRule {
    fun check(item: SeasonalCapacitySlot): MutableList<String>
}

data class SeasonalCapacitySlotCodeLengthRule(var maxLength: Int) : SeasonalCapacitySlotRule {
    override fun check(item: SeasonalCapacitySlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCapacitySlotAllowedCodeRule(var allowed: MutableList<String>) : SeasonalCapacitySlotRule {
    override fun check(item: SeasonalCapacitySlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCapacitySlotRequiredLabelRule(var message: String) : SeasonalCapacitySlotRule {
    override fun check(item: SeasonalCapacitySlot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCapacitySlotCodePrefixRule(var prefix: String) : SeasonalCapacitySlotRule {
    override fun check(item: SeasonalCapacitySlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal capacity slot. */
data class SeasonalCapacitySlotValidator(var rules: MutableList<SeasonalCapacitySlotRule>) {
    fun validate(item: SeasonalCapacitySlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalCapacitySlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalCapacitySlot>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalCapacitySlotValidator(): SeasonalCapacitySlotValidator {
    return SeasonalCapacitySlotValidator(
        mutableListOf<SeasonalCapacitySlotRule>(
            SeasonalCapacitySlotCodeLengthRule(255),
            SeasonalCapacitySlotAllowedCodeRule(mutableListOf<String>("regional", "primary")),
            SeasonalCapacitySlotRequiredLabelRule("seasonal capacity slot needs a value"),
            SeasonalCapacitySlotCodePrefixRule("X"),
        ),
    )
}
