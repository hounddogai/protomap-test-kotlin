package com.acme.transport.lastmile.attempt.manifest

/** A rule a attempt estimate must pass. Check returns its problems, or none. */
interface AttemptEstimateRule {
    fun check(item: AttemptEstimate): MutableList<String>
}

data class AttemptEstimateRequiredNameRule(var message: String) : AttemptEstimateRule {
    override fun check(item: AttemptEstimate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptEstimateRequiredCodeRule(var message: String) : AttemptEstimateRule {
    override fun check(item: AttemptEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptEstimateCodeLengthRule(var maxLength: Int) : AttemptEstimateRule {
    override fun check(item: AttemptEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptEstimateFloorValueRangeRule(var min: Double, var max: Double) : AttemptEstimateRule {
    override fun check(item: AttemptEstimate): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AttemptEstimateGraceDaysRangeRule(var min: Int, var max: Int) : AttemptEstimateRule {
    override fun check(item: AttemptEstimate): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt estimate. */
data class AttemptEstimateValidator(var rules: MutableList<AttemptEstimateRule>) {
    fun validate(item: AttemptEstimate): MutableList<String> {
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

    fun requireValid(item: AttemptEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptEstimateValidator(): AttemptEstimateValidator {
    return AttemptEstimateValidator(
        mutableListOf<AttemptEstimateRule>(
            AttemptEstimateRequiredNameRule("attempt estimate needs an entry"),
            AttemptEstimateRequiredCodeRule("attempt estimate needs this field"),
            AttemptEstimateCodeLengthRule(120),
            AttemptEstimateFloorValueRangeRule(0.6, 916.1),
            AttemptEstimateGraceDaysRangeRule(80, 1000),
        ),
    )
}
