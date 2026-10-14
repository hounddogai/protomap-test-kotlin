package com.acme.transport.freightpay.fuelsurcharge.mapping

import com.acme.transport.kit.text.normalize

/** A rule a rolling fuel surcharge preference must pass. Check returns its problems, or none. */
interface RollingFuelSurchargePreferenceRule {
    fun check(item: RollingFuelSurchargePreference): MutableList<String>
}

data class RollingFuelSurchargePreferenceRequiredCodeRule(
    var message: String,
) : RollingFuelSurchargePreferenceRule {
    override fun check(item: RollingFuelSurchargePreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelSurchargePreferenceStopCountRangeRule(
    var min: Int,
    var max: Int,
) : RollingFuelSurchargePreferenceRule {
    override fun check(item: RollingFuelSurchargePreference): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelSurchargePreferenceAllowedTitleRule(
    var allowed: MutableList<String>,
) : RollingFuelSurchargePreferenceRule {
    override fun check(item: RollingFuelSurchargePreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is too long")
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelSurchargePreferenceRequiredTitleRule(
    var message: String,
) : RollingFuelSurchargePreferenceRule {
    override fun check(item: RollingFuelSurchargePreference): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelSurchargePreferenceScoreRangeRule(
    var min: Double,
    var max: Double,
) : RollingFuelSurchargePreferenceRule {
    override fun check(item: RollingFuelSurchargePreference): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling fuel surcharge preference. */
data class RollingFuelSurchargePreferenceValidator(var rules: MutableList<RollingFuelSurchargePreferenceRule>) {
    fun validate(item: RollingFuelSurchargePreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingFuelSurchargePreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingFuelSurchargePreference>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingFuelSurchargePreferenceValidator(): RollingFuelSurchargePreferenceValidator {
    return RollingFuelSurchargePreferenceValidator(
        mutableListOf<RollingFuelSurchargePreferenceRule>(
            RollingFuelSurchargePreferenceRequiredCodeRule("rolling fuel surcharge preference needs an entry"),
            RollingFuelSurchargePreferenceStopCountRangeRule(120, 86400),
            RollingFuelSurchargePreferenceAllowedTitleRule(mutableListOf<String>("weighted")),
            RollingFuelSurchargePreferenceRequiredTitleRule("rolling fuel surcharge preference needs a value"),
            RollingFuelSurchargePreferenceScoreRangeRule(1.0, 2549.5),
        ),
    )
}
