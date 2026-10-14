package com.acme.transport.safety.violation.profile

import com.acme.transport.kit.text.normalize

/** A rule a rolling violation profile must pass. Check returns its problems, or none. */
interface RollingViolationProfileRule {
    fun check(item: RollingViolationProfile): MutableList<String>
}

data class RollingViolationProfileCodePrefixRule(var prefix: String) : RollingViolationProfileRule {
    override fun check(item: RollingViolationProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingViolationProfileRequiredCodeRule(var message: String) : RollingViolationProfileRule {
    override fun check(item: RollingViolationProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingViolationProfileDiscountRangeRule(
    var min: Double,
    var max: Double,
) : RollingViolationProfileRule {
    override fun check(item: RollingViolationProfile): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingViolationProfileRequiredTitleRule(var message: String) : RollingViolationProfileRule {
    override fun check(item: RollingViolationProfile): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingViolationProfileFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : RollingViolationProfileRule {
    override fun check(item: RollingViolationProfile): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingViolationProfileAllowedCodeRule(
    var allowed: MutableList<String>,
) : RollingViolationProfileRule {
    override fun check(item: RollingViolationProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling violation profile. */
data class RollingViolationProfileValidator(var rules: MutableList<RollingViolationProfileRule>) {
    fun validate(item: RollingViolationProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingViolationProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingViolationProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingViolationProfileValidator(): RollingViolationProfileValidator {
    return RollingViolationProfileValidator(
        mutableListOf<RollingViolationProfileRule>(
            RollingViolationProfileCodePrefixRule("ACM"),
            RollingViolationProfileRequiredCodeRule("rolling violation profile needs a value"),
            RollingViolationProfileDiscountRangeRule(8.7, 460.8),
            RollingViolationProfileRequiredTitleRule("rolling violation profile needs an entry"),
            RollingViolationProfileFuelLitersRangeRule(5.9, 4991.4),
            RollingViolationProfileAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
