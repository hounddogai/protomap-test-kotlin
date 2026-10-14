package com.acme.transport.linehaul.trailer.preference

/** A rule a seasonal trailer adjustment must pass. Check returns its problems, or none. */
interface SeasonalTrailerAdjustmentRule {
    fun check(item: SeasonalTrailerAdjustment): MutableList<String>
}

data class SeasonalTrailerAdjustmentLimitRangeRule(var min: Int, var max: Int) : SeasonalTrailerAdjustmentRule {
    override fun check(item: SeasonalTrailerAdjustment): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class SeasonalTrailerAdjustmentAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalTrailerAdjustmentRule {
    override fun check(item: SeasonalTrailerAdjustment): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerAdjustmentRequiredContactEmailRule(
    var message: String,
) : SeasonalTrailerAdjustmentRule {
    override fun check(item: SeasonalTrailerAdjustment): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerAdjustmentContactEmailLengthRule(var maxLength: Int) : SeasonalTrailerAdjustmentRule {
    override fun check(item: SeasonalTrailerAdjustment): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerAdjustmentCodeLengthRule(var maxLength: Int) : SeasonalTrailerAdjustmentRule {
    override fun check(item: SeasonalTrailerAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerAdjustmentTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : SeasonalTrailerAdjustmentRule {
    override fun check(item: SeasonalTrailerAdjustment): MutableList<String> {
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

/** Runs every rule of a seasonal trailer adjustment. */
data class SeasonalTrailerAdjustmentValidator(var rules: MutableList<SeasonalTrailerAdjustmentRule>) {
    fun validate(item: SeasonalTrailerAdjustment): MutableList<String> {
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

    fun requireValid(item: SeasonalTrailerAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTrailerAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTrailerAdjustmentValidator(): SeasonalTrailerAdjustmentValidator {
    return SeasonalTrailerAdjustmentValidator(
        mutableListOf<SeasonalTrailerAdjustmentRule>(
            SeasonalTrailerAdjustmentLimitRangeRule(64, 1000),
            SeasonalTrailerAdjustmentAttemptsRangeRule(1000, 3600),
            SeasonalTrailerAdjustmentRequiredContactEmailRule("seasonal trailer adjustment needs a value"),
            SeasonalTrailerAdjustmentContactEmailLengthRule(16),
            SeasonalTrailerAdjustmentCodeLengthRule(3600),
            SeasonalTrailerAdjustmentTagsLimitRule(16, mutableListOf<String>()),
        ),
    )
}
