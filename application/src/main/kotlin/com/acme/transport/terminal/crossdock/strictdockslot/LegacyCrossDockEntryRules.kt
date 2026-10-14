package com.acme.transport.terminal.crossdock.strictdockslot

/** A rule a legacy cross dock entry must pass. Check returns its problems, or none. */
interface LegacyCrossDockEntryRule {
    fun check(item: LegacyCrossDockEntry): MutableList<String>
}

data class LegacyCrossDockEntryDockLengthRule(var maxLength: Int) : LegacyCrossDockEntryRule {
    override fun check(item: LegacyCrossDockEntry): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCrossDockEntryRequiredCodeRule(var message: String) : LegacyCrossDockEntryRule {
    override fun check(item: LegacyCrossDockEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyCrossDockEntryFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : LegacyCrossDockEntryRule {
    override fun check(item: LegacyCrossDockEntry): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class LegacyCrossDockEntryEndsAtRangeRule(var min: Int, var max: Int) : LegacyCrossDockEntryRule {
    override fun check(item: LegacyCrossDockEntry): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy cross dock entry. */
data class LegacyCrossDockEntryValidator(var rules: MutableList<LegacyCrossDockEntryRule>) {
    fun validate(item: LegacyCrossDockEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyCrossDockEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyCrossDockEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyCrossDockEntryValidator(): LegacyCrossDockEntryValidator {
    return LegacyCrossDockEntryValidator(
        mutableListOf<LegacyCrossDockEntryRule>(
            LegacyCrossDockEntryDockLengthRule(32),
            LegacyCrossDockEntryRequiredCodeRule("legacy cross dock entry needs an entry"),
            LegacyCrossDockEntryFuelLitersRangeRule(0.9, 2499.6),
            LegacyCrossDockEntryEndsAtRangeRule(16, 1000),
        ),
    )
}
