package com.acme.transport.network.transittime.rollup

/** A rule a legacy transit time rollup must pass. Check returns its problems, or none. */
interface LegacyTransitTimeRollupRule {
    fun check(item: LegacyTransitTimeRollup): MutableList<String>
}

data class LegacyTransitTimeRollupRequiredLabelRule(var message: String) : LegacyTransitTimeRollupRule {
    override fun check(item: LegacyTransitTimeRollup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyTransitTimeRollupSequenceRangeRule(var min: Int, var max: Int) : LegacyTransitTimeRollupRule {
    override fun check(item: LegacyTransitTimeRollup): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTransitTimeRollupLabelLengthRule(var maxLength: Int) : LegacyTransitTimeRollupRule {
    override fun check(item: LegacyTransitTimeRollup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTransitTimeRollupCodeLengthRule(var maxLength: Int) : LegacyTransitTimeRollupRule {
    override fun check(item: LegacyTransitTimeRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTransitTimeRollupStartsAtRangeRule(var min: Int, var max: Int) : LegacyTransitTimeRollupRule {
    override fun check(item: LegacyTransitTimeRollup): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy transit time rollup. */
data class LegacyTransitTimeRollupValidator(var rules: MutableList<LegacyTransitTimeRollupRule>) {
    fun validate(item: LegacyTransitTimeRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyTransitTimeRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTransitTimeRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTransitTimeRollupValidator(): LegacyTransitTimeRollupValidator {
    return LegacyTransitTimeRollupValidator(
        mutableListOf<LegacyTransitTimeRollupRule>(
            LegacyTransitTimeRollupRequiredLabelRule("legacy transit time rollup needs an entry"),
            LegacyTransitTimeRollupSequenceRangeRule(80, 255),
            LegacyTransitTimeRollupLabelLengthRule(120),
            LegacyTransitTimeRollupCodeLengthRule(64),
            LegacyTransitTimeRollupStartsAtRangeRule(64, 80),
        ),
    )
}
