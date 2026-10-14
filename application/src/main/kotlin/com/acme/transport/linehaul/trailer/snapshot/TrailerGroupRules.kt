package com.acme.transport.linehaul.trailer.snapshot

/** A rule a trailer group must pass. Check returns its problems, or none. */
interface TrailerGroupRule {
    fun check(item: TrailerGroup): MutableList<String>
}

data class TrailerGroupStartsAtRangeRule(var min: Int, var max: Int) : TrailerGroupRule {
    override fun check(item: TrailerGroup): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class TrailerGroupEndsAtRangeRule(var min: Int, var max: Int) : TrailerGroupRule {
    override fun check(item: TrailerGroup): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class TrailerGroupRequiredNameRule(var message: String) : TrailerGroupRule {
    override fun check(item: TrailerGroup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerGroupMinimumRangeRule(var min: Double, var max: Double) : TrailerGroupRule {
    override fun check(item: TrailerGroup): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer group. */
data class TrailerGroupValidator(var rules: MutableList<TrailerGroupRule>) {
    fun validate(item: TrailerGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerGroupValidator(): TrailerGroupValidator {
    return TrailerGroupValidator(
        mutableListOf<TrailerGroupRule>(
            TrailerGroupStartsAtRangeRule(120, 86400),
            TrailerGroupEndsAtRangeRule(64, 255),
            TrailerGroupRequiredNameRule("trailer group needs an entry"),
            TrailerGroupMinimumRangeRule(5.8, 1242.4),
        ),
    )
}
