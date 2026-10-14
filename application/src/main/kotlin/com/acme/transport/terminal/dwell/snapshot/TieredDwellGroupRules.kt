package com.acme.transport.terminal.dwell.snapshot

/** A rule a tiered dwell group must pass. Check returns its problems, or none. */
interface TieredDwellGroupRule {
    fun check(item: TieredDwellGroup): MutableList<String>
}

data class TieredDwellGroupCostRangeRule(var min: Double, var max: Double) : TieredDwellGroupRule {
    override fun check(item: TieredDwellGroup): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class TieredDwellGroupRequiredNotesRule(var message: String) : TieredDwellGroupRule {
    override fun check(item: TieredDwellGroup): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDwellGroupExpiresAtRangeRule(var min: Int, var max: Int) : TieredDwellGroupRule {
    override fun check(item: TieredDwellGroup): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredDwellGroupNotesLengthRule(var maxLength: Int) : TieredDwellGroupRule {
    override fun check(item: TieredDwellGroup): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered dwell group. */
data class TieredDwellGroupValidator(var rules: MutableList<TieredDwellGroupRule>) {
    fun validate(item: TieredDwellGroup): MutableList<String> {
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

    fun requireValid(item: TieredDwellGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDwellGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDwellGroupValidator(): TieredDwellGroupValidator {
    return TieredDwellGroupValidator(
        mutableListOf<TieredDwellGroupRule>(
            TieredDwellGroupCostRangeRule(0.6, 3699.8),
            TieredDwellGroupRequiredNotesRule("tiered dwell group needs an entry"),
            TieredDwellGroupExpiresAtRangeRule(64, 64),
            TieredDwellGroupNotesLengthRule(80),
        ),
    )
}
