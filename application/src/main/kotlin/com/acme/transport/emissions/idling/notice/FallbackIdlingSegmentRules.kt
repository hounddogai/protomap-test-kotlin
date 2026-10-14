package com.acme.transport.emissions.idling.notice

import com.acme.transport.kit.text.normalize

/** A rule a fallback idling segment must pass. Check returns its problems, or none. */
interface FallbackIdlingSegmentRule {
    fun check(item: FallbackIdlingSegment): MutableList<String>
}

data class FallbackIdlingSegmentTitleLengthRule(var maxLength: Int) : FallbackIdlingSegmentRule {
    override fun check(item: FallbackIdlingSegment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackIdlingSegmentAllowedCodeRule(var allowed: MutableList<String>) : FallbackIdlingSegmentRule {
    override fun check(item: FallbackIdlingSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class FallbackIdlingSegmentCodeLengthRule(var maxLength: Int) : FallbackIdlingSegmentRule {
    override fun check(item: FallbackIdlingSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackIdlingSegmentMinimumRangeRule(var min: Double, var max: Double) : FallbackIdlingSegmentRule {
    override fun check(item: FallbackIdlingSegment): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback idling segment. */
data class FallbackIdlingSegmentValidator(var rules: MutableList<FallbackIdlingSegmentRule>) {
    fun validate(item: FallbackIdlingSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackIdlingSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackIdlingSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackIdlingSegmentValidator(): FallbackIdlingSegmentValidator {
    return FallbackIdlingSegmentValidator(
        mutableListOf<FallbackIdlingSegmentRule>(
            FallbackIdlingSegmentTitleLengthRule(86400),
            FallbackIdlingSegmentAllowedCodeRule(mutableListOf<String>("adaptive")),
            FallbackIdlingSegmentCodeLengthRule(16),
            FallbackIdlingSegmentMinimumRangeRule(9.8, 99.6),
        ),
    )
}
