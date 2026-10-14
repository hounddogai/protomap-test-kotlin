package com.acme.transport.crewing.swap.request

/** A rule a strict swap request must pass. Check returns its problems, or none. */
interface StrictSwapRequestRule {
    fun check(item: StrictSwapRequest): MutableList<String>
}

data class StrictSwapRequestCountryLengthRule(var maxLength: Int) : StrictSwapRequestRule {
    override fun check(item: StrictSwapRequest): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictSwapRequestPriorityRangeRule(var min: Int, var max: Int) : StrictSwapRequestRule {
    override fun check(item: StrictSwapRequest): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictSwapRequestExternalRefLengthRule(var maxLength: Int) : StrictSwapRequestRule {
    override fun check(item: StrictSwapRequest): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictSwapRequestTitleLengthRule(var maxLength: Int) : StrictSwapRequestRule {
    override fun check(item: StrictSwapRequest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict swap request. */
data class StrictSwapRequestValidator(var rules: MutableList<StrictSwapRequestRule>) {
    fun validate(item: StrictSwapRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictSwapRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictSwapRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictSwapRequestValidator(): StrictSwapRequestValidator {
    return StrictSwapRequestValidator(
        mutableListOf<StrictSwapRequestRule>(
            StrictSwapRequestCountryLengthRule(255),
            StrictSwapRequestPriorityRangeRule(32, 86400),
            StrictSwapRequestExternalRefLengthRule(64),
            StrictSwapRequestTitleLengthRule(16),
        ),
    )
}
