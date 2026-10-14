package com.acme.transport.linehaul.trailer.overrideitem

/** A rule a trailer cycle must pass. Check returns its problems, or none. */
interface TrailerCycleRule {
    fun check(item: TrailerCycle): MutableList<String>
}

data class TrailerCycleTitleLengthRule(var maxLength: Int) : TrailerCycleRule {
    override fun check(item: TrailerCycle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerCycleTrailerIdLengthRule(var maxLength: Int) : TrailerCycleRule {
    override fun check(item: TrailerCycle): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerCycleCostRangeRule(var min: Double, var max: Double) : TrailerCycleRule {
    override fun check(item: TrailerCycle): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class TrailerCycleCodePrefixRule(var prefix: String) : TrailerCycleRule {
    override fun check(item: TrailerCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerCycleCodeLengthRule(var maxLength: Int) : TrailerCycleRule {
    override fun check(item: TrailerCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerCycleRequiredTrailerIdRule(var message: String) : TrailerCycleRule {
    override fun check(item: TrailerCycle): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer cycle. */
data class TrailerCycleValidator(var rules: MutableList<TrailerCycleRule>) {
    fun validate(item: TrailerCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerCycleValidator(): TrailerCycleValidator {
    return TrailerCycleValidator(
        mutableListOf<TrailerCycleRule>(
            TrailerCycleTitleLengthRule(64),
            TrailerCycleTrailerIdLengthRule(255),
            TrailerCycleCostRangeRule(5.2, 2304.8),
            TrailerCycleCodePrefixRule("ACM"),
            TrailerCycleCodeLengthRule(64),
            TrailerCycleRequiredTrailerIdRule("trailer cycle needs an entry"),
        ),
    )
}
