package com.acme.transport.linehaul.trailer.budget

/** A rule a adaptive trailer request must pass. Check returns its problems, or none. */
interface AdaptiveTrailerRequestRule {
    fun check(item: AdaptiveTrailerRequest): MutableList<String>
}

data class AdaptiveTrailerRequestFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveTrailerRequestRule {
    override fun check(item: AdaptiveTrailerRequest): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class AdaptiveTrailerRequestRequiredTimezoneRule(var message: String) : AdaptiveTrailerRequestRule {
    override fun check(item: AdaptiveTrailerRequest): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTrailerRequestNameLengthRule(var maxLength: Int) : AdaptiveTrailerRequestRule {
    override fun check(item: AdaptiveTrailerRequest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive trailer request. */
data class AdaptiveTrailerRequestValidator(var rules: MutableList<AdaptiveTrailerRequestRule>) {
    fun validate(item: AdaptiveTrailerRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveTrailerRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveTrailerRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveTrailerRequestValidator(): AdaptiveTrailerRequestValidator {
    return AdaptiveTrailerRequestValidator(
        mutableListOf<AdaptiveTrailerRequestRule>(
            AdaptiveTrailerRequestFloorValueRangeRule(7.1, 3519.1),
            AdaptiveTrailerRequestRequiredTimezoneRule("adaptive trailer request needs this field"),
            AdaptiveTrailerRequestNameLengthRule(32),
        ),
    )
}
