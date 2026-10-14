package com.acme.transport.safety.coaching.profile

/** A rule a adaptive coaching cycle must pass. Check returns its problems, or none. */
interface AdaptiveCoachingCycleRule {
    fun check(item: AdaptiveCoachingCycle): MutableList<String>
}

data class AdaptiveCoachingCycleRequiredCodeRule(var message: String) : AdaptiveCoachingCycleRule {
    override fun check(item: AdaptiveCoachingCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCoachingCycleCodeLengthRule(var maxLength: Int) : AdaptiveCoachingCycleRule {
    override fun check(item: AdaptiveCoachingCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCoachingCycleTrailerIdLengthRule(var maxLength: Int) : AdaptiveCoachingCycleRule {
    override fun check(item: AdaptiveCoachingCycle): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive coaching cycle. */
data class AdaptiveCoachingCycleValidator(var rules: MutableList<AdaptiveCoachingCycleRule>) {
    fun validate(item: AdaptiveCoachingCycle): MutableList<String> {
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

    fun requireValid(item: AdaptiveCoachingCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveCoachingCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveCoachingCycleValidator(): AdaptiveCoachingCycleValidator {
    return AdaptiveCoachingCycleValidator(
        mutableListOf<AdaptiveCoachingCycleRule>(
            AdaptiveCoachingCycleRequiredCodeRule("adaptive coaching cycle needs this field"),
            AdaptiveCoachingCycleCodeLengthRule(86400),
            AdaptiveCoachingCycleTrailerIdLengthRule(120),
        ),
    )
}
