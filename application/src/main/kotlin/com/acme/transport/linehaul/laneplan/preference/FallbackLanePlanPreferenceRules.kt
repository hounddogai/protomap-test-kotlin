package com.acme.transport.linehaul.laneplan.preference

/** A rule a fallback lane plan preference must pass. Check returns its problems, or none. */
interface FallbackLanePlanPreferenceRule {
    fun check(item: FallbackLanePlanPreference): MutableList<String>
}

data class FallbackLanePlanPreferenceRequiredCategoryRule(
    var message: String,
) : FallbackLanePlanPreferenceRule {
    override fun check(item: FallbackLanePlanPreference): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackLanePlanPreferenceFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : FallbackLanePlanPreferenceRule {
    override fun check(item: FallbackLanePlanPreference): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLanePlanPreferenceCostRangeRule(
    var min: Double,
    var max: Double,
) : FallbackLanePlanPreferenceRule {
    override fun check(item: FallbackLanePlanPreference): MutableList<String> {
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

/** Runs every rule of a fallback lane plan preference. */
data class FallbackLanePlanPreferenceValidator(var rules: MutableList<FallbackLanePlanPreferenceRule>) {
    fun validate(item: FallbackLanePlanPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackLanePlanPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackLanePlanPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackLanePlanPreferenceValidator(): FallbackLanePlanPreferenceValidator {
    return FallbackLanePlanPreferenceValidator(
        mutableListOf<FallbackLanePlanPreferenceRule>(
            FallbackLanePlanPreferenceRequiredCategoryRule("fallback lane plan preference needs an entry"),
            FallbackLanePlanPreferenceFloorValueRangeRule(6.0, 1054.6),
            FallbackLanePlanPreferenceCostRangeRule(6.7, 3946.7),
        ),
    )
}
