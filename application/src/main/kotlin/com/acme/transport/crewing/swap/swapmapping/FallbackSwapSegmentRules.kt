package com.acme.transport.crewing.swap.swapmapping

/** A rule a fallback swap segment must pass. Check returns its problems, or none. */
interface FallbackSwapSegmentRule {
    fun check(item: FallbackSwapSegment): MutableList<String>
}

data class FallbackSwapSegmentRequiredCategoryRule(var message: String) : FallbackSwapSegmentRule {
    override fun check(item: FallbackSwapSegment): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackSwapSegmentRequiredCodeRule(var message: String) : FallbackSwapSegmentRule {
    override fun check(item: FallbackSwapSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackSwapSegmentRequiredUnitRule(var message: String) : FallbackSwapSegmentRule {
    override fun check(item: FallbackSwapSegment): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback swap segment. */
data class FallbackSwapSegmentValidator(var rules: MutableList<FallbackSwapSegmentRule>) {
    fun validate(item: FallbackSwapSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackSwapSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackSwapSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackSwapSegmentValidator(): FallbackSwapSegmentValidator {
    return FallbackSwapSegmentValidator(
        mutableListOf<FallbackSwapSegmentRule>(
            FallbackSwapSegmentRequiredCategoryRule("fallback swap segment needs a value"),
            FallbackSwapSegmentRequiredCodeRule("fallback swap segment needs a value"),
            FallbackSwapSegmentRequiredUnitRule("fallback swap segment needs an entry"),
        ),
    )
}
