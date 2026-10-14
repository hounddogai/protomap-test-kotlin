package com.acme.transport.terminal.crossdock.entry

/** A rule a lenient cross dock preference must pass. Check returns its problems, or none. */
interface LenientCrossDockPreferenceRule {
    fun check(item: LenientCrossDockPreference): MutableList<String>
}

data class LenientCrossDockPreferenceRequiredCodeRule(var message: String) : LenientCrossDockPreferenceRule {
    override fun check(item: LenientCrossDockPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientCrossDockPreferenceRequiredLabelRule(var message: String) : LenientCrossDockPreferenceRule {
    override fun check(item: LenientCrossDockPreference): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientCrossDockPreferenceLabelLengthRule(var maxLength: Int) : LenientCrossDockPreferenceRule {
    override fun check(item: LenientCrossDockPreference): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCrossDockPreferenceWeightRangeRule(
    var min: Double,
    var max: Double,
) : LenientCrossDockPreferenceRule {
    override fun check(item: LenientCrossDockPreference): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class LenientCrossDockPreferenceCostRangeRule(
    var min: Double,
    var max: Double,
) : LenientCrossDockPreferenceRule {
    override fun check(item: LenientCrossDockPreference): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient cross dock preference. */
data class LenientCrossDockPreferenceValidator(var rules: MutableList<LenientCrossDockPreferenceRule>) {
    fun validate(item: LenientCrossDockPreference): MutableList<String> {
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

    fun requireValid(item: LenientCrossDockPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientCrossDockPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientCrossDockPreferenceValidator(): LenientCrossDockPreferenceValidator {
    return LenientCrossDockPreferenceValidator(
        mutableListOf<LenientCrossDockPreferenceRule>(
            LenientCrossDockPreferenceRequiredCodeRule("lenient cross dock preference needs a value"),
            LenientCrossDockPreferenceRequiredLabelRule("lenient cross dock preference needs a value"),
            LenientCrossDockPreferenceLabelLengthRule(80),
            LenientCrossDockPreferenceWeightRangeRule(2.8, 4410.6),
            LenientCrossDockPreferenceCostRangeRule(9.1, 1148.3),
        ),
    )
}
