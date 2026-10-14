package com.acme.transport.crewing.swap.recorditem

/** A rule a legacy swap score must pass. Check returns its problems, or none. */
interface LegacySwapScoreRule {
    fun check(item: LegacySwapScore): MutableList<String>
}

data class LegacySwapScoreCodePrefixRule(var prefix: String) : LegacySwapScoreRule {
    override fun check(item: LegacySwapScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacySwapScoreDescriptionLengthRule(var maxLength: Int) : LegacySwapScoreRule {
    override fun check(item: LegacySwapScore): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacySwapScoreTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : LegacySwapScoreRule {
    override fun check(item: LegacySwapScore): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class LegacySwapScoreCodeLengthRule(var maxLength: Int) : LegacySwapScoreRule {
    override fun check(item: LegacySwapScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacySwapScoreCapacityRangeRule(var min: Int, var max: Int) : LegacySwapScoreRule {
    override fun check(item: LegacySwapScore): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy swap score. */
data class LegacySwapScoreValidator(var rules: MutableList<LegacySwapScoreRule>) {
    fun validate(item: LegacySwapScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacySwapScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacySwapScore>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacySwapScoreValidator(): LegacySwapScoreValidator {
    return LegacySwapScoreValidator(
        mutableListOf<LegacySwapScoreRule>(
            LegacySwapScoreCodePrefixRule("OPS"),
            LegacySwapScoreDescriptionLengthRule(1000),
            LegacySwapScoreTagsLimitRule(16, mutableListOf<String>("scheduled", "tiered", "seasonal")),
            LegacySwapScoreCodeLengthRule(86400),
            LegacySwapScoreCapacityRangeRule(80, 120),
        ),
    )
}
