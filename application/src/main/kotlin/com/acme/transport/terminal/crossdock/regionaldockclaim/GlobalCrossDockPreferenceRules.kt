package com.acme.transport.terminal.crossdock.regionaldockclaim

/** A rule a global cross dock preference must pass. Check returns its problems, or none. */
interface GlobalCrossDockPreferenceRule {
    fun check(item: GlobalCrossDockPreference): MutableList<String>
}

data class GlobalCrossDockPreferenceLabelLengthRule(var maxLength: Int) : GlobalCrossDockPreferenceRule {
    override fun check(item: GlobalCrossDockPreference): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCrossDockPreferenceCeilingRangeRule(
    var min: Double,
    var max: Double,
) : GlobalCrossDockPreferenceRule {
    override fun check(item: GlobalCrossDockPreference): MutableList<String> {
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

data class GlobalCrossDockPreferenceSegmentLengthRule(var maxLength: Int) : GlobalCrossDockPreferenceRule {
    override fun check(item: GlobalCrossDockPreference): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCrossDockPreferenceFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : GlobalCrossDockPreferenceRule {
    override fun check(item: GlobalCrossDockPreference): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global cross dock preference. */
data class GlobalCrossDockPreferenceValidator(var rules: MutableList<GlobalCrossDockPreferenceRule>) {
    fun validate(item: GlobalCrossDockPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalCrossDockPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalCrossDockPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalCrossDockPreferenceValidator(): GlobalCrossDockPreferenceValidator {
    return GlobalCrossDockPreferenceValidator(
        mutableListOf<GlobalCrossDockPreferenceRule>(
            GlobalCrossDockPreferenceLabelLengthRule(86400),
            GlobalCrossDockPreferenceCeilingRangeRule(2.7, 1320.2),
            GlobalCrossDockPreferenceSegmentLengthRule(1000),
            GlobalCrossDockPreferenceFuelLitersRangeRule(1.9, 3313.8),
        ),
    )
}
