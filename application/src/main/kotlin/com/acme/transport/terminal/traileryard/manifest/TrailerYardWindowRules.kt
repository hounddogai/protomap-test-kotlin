package com.acme.transport.terminal.traileryard.manifest

/** A rule a trailer yard window must pass. Check returns its problems, or none. */
interface TrailerYardWindowRule {
    fun check(item: TrailerYardWindow): MutableList<String>
}

data class TrailerYardWindowDurationMinutesRangeRule(var min: Int, var max: Int) : TrailerYardWindowRule {
    override fun check(item: TrailerYardWindow): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardWindowReviewerLengthRule(var maxLength: Int) : TrailerYardWindowRule {
    override fun check(item: TrailerYardWindow): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardWindowRequiredNameRule(var message: String) : TrailerYardWindowRule {
    override fun check(item: TrailerYardWindow): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardWindowAmountRangeRule(var min: Double, var max: Double) : TrailerYardWindowRule {
    override fun check(item: TrailerYardWindow): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard window. */
data class TrailerYardWindowValidator(var rules: MutableList<TrailerYardWindowRule>) {
    fun validate(item: TrailerYardWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerYardWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardWindowValidator(): TrailerYardWindowValidator {
    return TrailerYardWindowValidator(
        mutableListOf<TrailerYardWindowRule>(
            TrailerYardWindowDurationMinutesRangeRule(32, 3600),
            TrailerYardWindowReviewerLengthRule(3600),
            TrailerYardWindowRequiredNameRule("trailer yard window needs this field"),
            TrailerYardWindowAmountRangeRule(7.8, 1717.8),
        ),
    )
}
