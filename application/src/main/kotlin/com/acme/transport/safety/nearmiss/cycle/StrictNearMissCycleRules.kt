package com.acme.transport.safety.nearmiss.cycle

/** A rule a strict near miss cycle must pass. Check returns its problems, or none. */
interface StrictNearMissCycleRule {
    fun check(item: StrictNearMissCycle): MutableList<String>
}

data class StrictNearMissCycleTitleLengthRule(var maxLength: Int) : StrictNearMissCycleRule {
    override fun check(item: StrictNearMissCycle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictNearMissCycleCodeLengthRule(var maxLength: Int) : StrictNearMissCycleRule {
    override fun check(item: StrictNearMissCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictNearMissCycleTrailerIdLengthRule(var maxLength: Int) : StrictNearMissCycleRule {
    override fun check(item: StrictNearMissCycle): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictNearMissCycleCodePrefixRule(var prefix: String) : StrictNearMissCycleRule {
    override fun check(item: StrictNearMissCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictNearMissCycleCurrencyLengthRule(var maxLength: Int) : StrictNearMissCycleRule {
    override fun check(item: StrictNearMissCycle): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictNearMissCycleScoreRangeRule(var min: Double, var max: Double) : StrictNearMissCycleRule {
    override fun check(item: StrictNearMissCycle): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict near miss cycle. */
data class StrictNearMissCycleValidator(var rules: MutableList<StrictNearMissCycleRule>) {
    fun validate(item: StrictNearMissCycle): MutableList<String> {
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

    fun requireValid(item: StrictNearMissCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictNearMissCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictNearMissCycleValidator(): StrictNearMissCycleValidator {
    return StrictNearMissCycleValidator(
        mutableListOf<StrictNearMissCycleRule>(
            StrictNearMissCycleTitleLengthRule(16),
            StrictNearMissCycleCodeLengthRule(120),
            StrictNearMissCycleTrailerIdLengthRule(3600),
            StrictNearMissCycleCodePrefixRule("REF"),
            StrictNearMissCycleCurrencyLengthRule(32),
            StrictNearMissCycleScoreRangeRule(2.9, 522.6),
        ),
    )
}
