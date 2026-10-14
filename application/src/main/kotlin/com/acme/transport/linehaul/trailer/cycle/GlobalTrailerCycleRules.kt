package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.kit.text.normalize

/** A rule a global trailer cycle must pass. Check returns its problems, or none. */
interface GlobalTrailerCycleRule {
    fun check(item: GlobalTrailerCycle): MutableList<String>
}

data class GlobalTrailerCycleRequiredCountryRule(var message: String) : GlobalTrailerCycleRule {
    override fun check(item: GlobalTrailerCycle): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerCycleAttemptsRangeRule(var min: Int, var max: Int) : GlobalTrailerCycleRule {
    override fun check(item: GlobalTrailerCycle): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class GlobalTrailerCycleAllowedCountryRule(var allowed: MutableList<String>) : GlobalTrailerCycleRule {
    override fun check(item: GlobalTrailerCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global trailer cycle. */
data class GlobalTrailerCycleValidator(var rules: MutableList<GlobalTrailerCycleRule>) {
    fun validate(item: GlobalTrailerCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalTrailerCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTrailerCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTrailerCycleValidator(): GlobalTrailerCycleValidator {
    return GlobalTrailerCycleValidator(
        mutableListOf<GlobalTrailerCycleRule>(
            GlobalTrailerCycleRequiredCountryRule("global trailer cycle needs an entry"),
            GlobalTrailerCycleAttemptsRangeRule(16, 255),
            GlobalTrailerCycleAllowedCountryRule(mutableListOf<String>("global")),
        ),
    )
}
