package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.kit.text.normalize

/** A rule a seasonal service area exception must pass. Check returns its problems, or none. */
interface SeasonalServiceAreaExceptionRule {
    fun check(item: SeasonalServiceAreaException): MutableList<String>
}

data class SeasonalServiceAreaExceptionBudgetRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalServiceAreaExceptionRule {
    override fun check(item: SeasonalServiceAreaException): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServiceAreaExceptionCodePrefixRule(var prefix: String) : SeasonalServiceAreaExceptionRule {
    override fun check(item: SeasonalServiceAreaException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServiceAreaExceptionRequiredCodeRule(
    var message: String,
) : SeasonalServiceAreaExceptionRule {
    override fun check(item: SeasonalServiceAreaException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServiceAreaExceptionAllowedNameRule(
    var allowed: MutableList<String>,
) : SeasonalServiceAreaExceptionRule {
    override fun check(item: SeasonalServiceAreaException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is expired")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServiceAreaExceptionEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalServiceAreaExceptionRule {
    override fun check(item: SeasonalServiceAreaException): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class SeasonalServiceAreaExceptionCodeLengthRule(var maxLength: Int) : SeasonalServiceAreaExceptionRule {
    override fun check(item: SeasonalServiceAreaException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal service area exception. */
data class SeasonalServiceAreaExceptionValidator(var rules: MutableList<SeasonalServiceAreaExceptionRule>) {
    fun validate(item: SeasonalServiceAreaException): MutableList<String> {
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

    fun requireValid(item: SeasonalServiceAreaException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalServiceAreaException>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalServiceAreaExceptionValidator(): SeasonalServiceAreaExceptionValidator {
    return SeasonalServiceAreaExceptionValidator(
        mutableListOf<SeasonalServiceAreaExceptionRule>(
            SeasonalServiceAreaExceptionBudgetRangeRule(4.8, 3227.0),
            SeasonalServiceAreaExceptionCodePrefixRule("ACM"),
            SeasonalServiceAreaExceptionRequiredCodeRule("seasonal service area exception needs an entry"),
            SeasonalServiceAreaExceptionAllowedNameRule(mutableListOf<String>("primary")),
            SeasonalServiceAreaExceptionEtaMinutesRangeRule(3600, 3600),
            SeasonalServiceAreaExceptionCodeLengthRule(120),
        ),
    )
}
