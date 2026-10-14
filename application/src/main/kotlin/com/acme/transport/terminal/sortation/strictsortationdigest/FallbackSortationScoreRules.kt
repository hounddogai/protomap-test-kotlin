package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.kit.text.normalize

/** A rule a fallback sortation score must pass. Check returns its problems, or none. */
interface FallbackSortationScoreRule {
    fun check(item: FallbackSortationScore): MutableList<String>
}

data class FallbackSortationScoreCodeLengthRule(var maxLength: Int) : FallbackSortationScoreRule {
    override fun check(item: FallbackSortationScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackSortationScoreVisibleGuardRule(var limit: Double) : FallbackSortationScoreRule {
    override fun check(item: FallbackSortationScore): MutableList<String> {
        if (item.visible && item.maximum > limit) {
            return mutableListOf<String>("maximum ${item.maximum} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class FallbackSortationScoreAllowedTrailerIdRule(
    var allowed: MutableList<String>,
) : FallbackSortationScoreRule {
    override fun check(item: FallbackSortationScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class FallbackSortationScoreRequiredTrailerIdRule(var message: String) : FallbackSortationScoreRule {
    override fun check(item: FallbackSortationScore): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackSortationScoreRequiredLabelRule(var message: String) : FallbackSortationScoreRule {
    override fun check(item: FallbackSortationScore): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback sortation score. */
data class FallbackSortationScoreValidator(var rules: MutableList<FallbackSortationScoreRule>) {
    fun validate(item: FallbackSortationScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackSortationScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackSortationScore>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackSortationScoreValidator(): FallbackSortationScoreValidator {
    return FallbackSortationScoreValidator(
        mutableListOf<FallbackSortationScoreRule>(
            FallbackSortationScoreCodeLengthRule(32),
            FallbackSortationScoreVisibleGuardRule(8.9),
            FallbackSortationScoreAllowedTrailerIdRule(mutableListOf<String>("default")),
            FallbackSortationScoreRequiredTrailerIdRule("fallback sortation score needs an entry"),
            FallbackSortationScoreRequiredLabelRule("fallback sortation score needs a value"),
        ),
    )
}
