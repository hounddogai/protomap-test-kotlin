package com.acme.transport.crewing.restbreak.entry

import com.acme.transport.kit.text.normalize

/** A rule a rest break cycle must pass. Check returns its problems, or none. */
interface RestBreakCycleRule {
    fun check(item: RestBreakCycle): MutableList<String>
}

data class RestBreakCycleRequiredLaneRule(var message: String) : RestBreakCycleRule {
    override fun check(item: RestBreakCycle): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakCycleRequiredCodeRule(var message: String) : RestBreakCycleRule {
    override fun check(item: RestBreakCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakCycleLaneLengthRule(var maxLength: Int) : RestBreakCycleRule {
    override fun check(item: RestBreakCycle): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakCycleAllowedTitleRule(var allowed: MutableList<String>) : RestBreakCycleRule {
    override fun check(item: RestBreakCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break cycle. */
data class RestBreakCycleValidator(var rules: MutableList<RestBreakCycleRule>) {
    fun validate(item: RestBreakCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RestBreakCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakCycleValidator(): RestBreakCycleValidator {
    return RestBreakCycleValidator(
        mutableListOf<RestBreakCycleRule>(
            RestBreakCycleRequiredLaneRule("rest break cycle needs a value"),
            RestBreakCycleRequiredCodeRule("rest break cycle needs an entry"),
            RestBreakCycleLaneLengthRule(3600),
            RestBreakCycleAllowedTitleRule(mutableListOf<String>()),
        ),
    )
}
