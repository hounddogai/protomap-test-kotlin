package com.acme.transport.emissions.idling.group

import com.acme.transport.kit.text.normalize

/** A rule a rolling idling request must pass. Check returns its problems, or none. */
interface RollingIdlingRequestRule {
    fun check(item: RollingIdlingRequest): MutableList<String>
}

data class RollingIdlingRequestPhoneLengthRule(var maxLength: Int) : RollingIdlingRequestRule {
    override fun check(item: RollingIdlingRequest): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingIdlingRequestRouteCodeLengthRule(var maxLength: Int) : RollingIdlingRequestRule {
    override fun check(item: RollingIdlingRequest): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingIdlingRequestAllowedPhoneRule(var allowed: MutableList<String>) : RollingIdlingRequestRule {
    override fun check(item: RollingIdlingRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class RollingIdlingRequestRequiredTitleRule(var message: String) : RollingIdlingRequestRule {
    override fun check(item: RollingIdlingRequest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingIdlingRequestThresholdRangeRule(var min: Double, var max: Double) : RollingIdlingRequestRule {
    override fun check(item: RollingIdlingRequest): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling idling request. */
data class RollingIdlingRequestValidator(var rules: MutableList<RollingIdlingRequestRule>) {
    fun validate(item: RollingIdlingRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingIdlingRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingIdlingRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingIdlingRequestValidator(): RollingIdlingRequestValidator {
    return RollingIdlingRequestValidator(
        mutableListOf<RollingIdlingRequestRule>(
            RollingIdlingRequestPhoneLengthRule(86400),
            RollingIdlingRequestRouteCodeLengthRule(120),
            RollingIdlingRequestAllowedPhoneRule(mutableListOf<String>("pending", "weighted", "capped")),
            RollingIdlingRequestRequiredTitleRule("rolling idling request needs a value"),
            RollingIdlingRequestThresholdRangeRule(7.6, 229.9),
        ),
    )
}
