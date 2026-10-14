package com.acme.transport.crewing.restbreak.regionalbreakpreference

/** A rule a weighted rest break preference must pass. Check returns its problems, or none. */
interface WeightedRestBreakPreferenceRule {
    fun check(item: WeightedRestBreakPreference): MutableList<String>
}

data class WeightedRestBreakPreferenceNameLengthRule(var maxLength: Int) : WeightedRestBreakPreferenceRule {
    override fun check(item: WeightedRestBreakPreference): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRestBreakPreferenceRequiredNameRule(var message: String) : WeightedRestBreakPreferenceRule {
    override fun check(item: WeightedRestBreakPreference): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedRestBreakPreferenceRequiredCategoryRule(
    var message: String,
) : WeightedRestBreakPreferenceRule {
    override fun check(item: WeightedRestBreakPreference): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedRestBreakPreferenceDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : WeightedRestBreakPreferenceRule {
    override fun check(item: WeightedRestBreakPreference): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class WeightedRestBreakPreferenceCodeLengthRule(var maxLength: Int) : WeightedRestBreakPreferenceRule {
    override fun check(item: WeightedRestBreakPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted rest break preference. */
data class WeightedRestBreakPreferenceValidator(var rules: MutableList<WeightedRestBreakPreferenceRule>) {
    fun validate(item: WeightedRestBreakPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedRestBreakPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedRestBreakPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedRestBreakPreferenceValidator(): WeightedRestBreakPreferenceValidator {
    return WeightedRestBreakPreferenceValidator(
        mutableListOf<WeightedRestBreakPreferenceRule>(
            WeightedRestBreakPreferenceNameLengthRule(86400),
            WeightedRestBreakPreferenceRequiredNameRule("weighted rest break preference needs an entry"),
            WeightedRestBreakPreferenceRequiredCategoryRule("weighted rest break preference needs a value"),
            WeightedRestBreakPreferenceDistanceKmRangeRule(2.4, 2389.4),
            WeightedRestBreakPreferenceCodeLengthRule(255),
        ),
    )
}
