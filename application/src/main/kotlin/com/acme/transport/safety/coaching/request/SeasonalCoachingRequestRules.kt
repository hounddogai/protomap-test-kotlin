package com.acme.transport.safety.coaching.request

/** A rule a seasonal coaching request must pass. Check returns its problems, or none. */
interface SeasonalCoachingRequestRule {
    fun check(item: SeasonalCoachingRequest): MutableList<String>
}

data class SeasonalCoachingRequestRequiredCodeRule(var message: String) : SeasonalCoachingRequestRule {
    override fun check(item: SeasonalCoachingRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCoachingRequestCodePrefixRule(var prefix: String) : SeasonalCoachingRequestRule {
    override fun check(item: SeasonalCoachingRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCoachingRequestTitleLengthRule(var maxLength: Int) : SeasonalCoachingRequestRule {
    override fun check(item: SeasonalCoachingRequest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCoachingRequestStopCountRangeRule(var min: Int, var max: Int) : SeasonalCoachingRequestRule {
    override fun check(item: SeasonalCoachingRequest): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal coaching request. */
data class SeasonalCoachingRequestValidator(var rules: MutableList<SeasonalCoachingRequestRule>) {
    fun validate(item: SeasonalCoachingRequest): MutableList<String> {
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

    fun requireValid(item: SeasonalCoachingRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalCoachingRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalCoachingRequestValidator(): SeasonalCoachingRequestValidator {
    return SeasonalCoachingRequestValidator(
        mutableListOf<SeasonalCoachingRequestRule>(
            SeasonalCoachingRequestRequiredCodeRule("seasonal coaching request needs an entry"),
            SeasonalCoachingRequestCodePrefixRule("OPS"),
            SeasonalCoachingRequestTitleLengthRule(80),
            SeasonalCoachingRequestStopCountRangeRule(1000, 86400),
        ),
    )
}
