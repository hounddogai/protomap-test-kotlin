package com.acme.transport.terminal.sortation.sortationpreference

/** A rule a rolling sortation preference must pass. Check returns its problems, or none. */
interface RollingSortationPreferenceRule {
    fun check(item: RollingSortationPreference): MutableList<String>
}

data class RollingSortationPreferenceLaneLengthRule(var maxLength: Int) : RollingSortationPreferenceRule {
    override fun check(item: RollingSortationPreference): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingSortationPreferenceRequiredLaneRule(var message: String) : RollingSortationPreferenceRule {
    override fun check(item: RollingSortationPreference): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingSortationPreferenceOdometerRangeRule(
    var min: Int,
    var max: Int,
) : RollingSortationPreferenceRule {
    override fun check(item: RollingSortationPreference): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingSortationPreferenceRequiredTitleRule(var message: String) : RollingSortationPreferenceRule {
    override fun check(item: RollingSortationPreference): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingSortationPreferenceCapacityRangeRule(
    var min: Int,
    var max: Int,
) : RollingSortationPreferenceRule {
    override fun check(item: RollingSortationPreference): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class RollingSortationPreferenceRequiredCodeRule(var message: String) : RollingSortationPreferenceRule {
    override fun check(item: RollingSortationPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling sortation preference. */
data class RollingSortationPreferenceValidator(var rules: MutableList<RollingSortationPreferenceRule>) {
    fun validate(item: RollingSortationPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingSortationPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingSortationPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingSortationPreferenceValidator(): RollingSortationPreferenceValidator {
    return RollingSortationPreferenceValidator(
        mutableListOf<RollingSortationPreferenceRule>(
            RollingSortationPreferenceLaneLengthRule(86400),
            RollingSortationPreferenceRequiredLaneRule("rolling sortation preference needs an entry"),
            RollingSortationPreferenceOdometerRangeRule(16, 1000),
            RollingSortationPreferenceRequiredTitleRule("rolling sortation preference needs this field"),
            RollingSortationPreferenceCapacityRangeRule(255, 255),
            RollingSortationPreferenceRequiredCodeRule("rolling sortation preference needs a value"),
        ),
    )
}
