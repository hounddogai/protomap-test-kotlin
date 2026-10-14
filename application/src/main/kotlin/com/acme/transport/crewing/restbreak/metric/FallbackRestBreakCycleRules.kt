package com.acme.transport.crewing.restbreak.metric

import com.acme.transport.kit.text.normalize

/** A rule a fallback rest break cycle must pass. Check returns its problems, or none. */
interface FallbackRestBreakCycleRule {
    fun check(item: FallbackRestBreakCycle): MutableList<String>
}

data class FallbackRestBreakCycleRevisionRangeRule(var min: Int, var max: Int) : FallbackRestBreakCycleRule {
    override fun check(item: FallbackRestBreakCycle): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRestBreakCycleRatioRangeRule(var min: Double, var max: Double) : FallbackRestBreakCycleRule {
    override fun check(item: FallbackRestBreakCycle): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class FallbackRestBreakCycleCreatedAtRangeRule(var min: Int, var max: Int) : FallbackRestBreakCycleRule {
    override fun check(item: FallbackRestBreakCycle): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRestBreakCycleNameLengthRule(var maxLength: Int) : FallbackRestBreakCycleRule {
    override fun check(item: FallbackRestBreakCycle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRestBreakCycleAllowedCodeRule(
    var allowed: MutableList<String>,
) : FallbackRestBreakCycleRule {
    override fun check(item: FallbackRestBreakCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRestBreakCycleRequiredCodeRule(var message: String) : FallbackRestBreakCycleRule {
    override fun check(item: FallbackRestBreakCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback rest break cycle. */
data class FallbackRestBreakCycleValidator(var rules: MutableList<FallbackRestBreakCycleRule>) {
    fun validate(item: FallbackRestBreakCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackRestBreakCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRestBreakCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRestBreakCycleValidator(): FallbackRestBreakCycleValidator {
    return FallbackRestBreakCycleValidator(
        mutableListOf<FallbackRestBreakCycleRule>(
            FallbackRestBreakCycleRevisionRangeRule(32, 1000),
            FallbackRestBreakCycleRatioRangeRule(3.6, 4663.7),
            FallbackRestBreakCycleCreatedAtRangeRule(255, 3600),
            FallbackRestBreakCycleNameLengthRule(3600),
            FallbackRestBreakCycleAllowedCodeRule(mutableListOf<String>("default")),
            FallbackRestBreakCycleRequiredCodeRule("fallback rest break cycle needs an entry"),
        ),
    )
}
