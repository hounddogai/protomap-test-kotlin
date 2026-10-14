package com.acme.transport.terminal.sortation.adjustment

/** A rule a seasonal sortation adjustment must pass. Check returns its problems, or none. */
interface SeasonalSortationAdjustmentRule {
    fun check(item: SeasonalSortationAdjustment): MutableList<String>
}

data class SeasonalSortationAdjustmentLocaleLengthRule(var maxLength: Int) : SeasonalSortationAdjustmentRule {
    override fun check(item: SeasonalSortationAdjustment): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalSortationAdjustmentExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalSortationAdjustmentRule {
    override fun check(item: SeasonalSortationAdjustment): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalSortationAdjustmentRequiredNameRule(var message: String) : SeasonalSortationAdjustmentRule {
    override fun check(item: SeasonalSortationAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalSortationAdjustmentCodeLengthRule(var maxLength: Int) : SeasonalSortationAdjustmentRule {
    override fun check(item: SeasonalSortationAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalSortationAdjustmentRequiredCodeRule(var message: String) : SeasonalSortationAdjustmentRule {
    override fun check(item: SeasonalSortationAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalSortationAdjustmentTimezoneLengthRule(var maxLength: Int) : SeasonalSortationAdjustmentRule {
    override fun check(item: SeasonalSortationAdjustment): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal sortation adjustment. */
data class SeasonalSortationAdjustmentValidator(var rules: MutableList<SeasonalSortationAdjustmentRule>) {
    fun validate(item: SeasonalSortationAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalSortationAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalSortationAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalSortationAdjustmentValidator(): SeasonalSortationAdjustmentValidator {
    return SeasonalSortationAdjustmentValidator(
        mutableListOf<SeasonalSortationAdjustmentRule>(
            SeasonalSortationAdjustmentLocaleLengthRule(3600),
            SeasonalSortationAdjustmentExpiresAtRangeRule(255, 255),
            SeasonalSortationAdjustmentRequiredNameRule("seasonal sortation adjustment needs a value"),
            SeasonalSortationAdjustmentCodeLengthRule(120),
            SeasonalSortationAdjustmentRequiredCodeRule("seasonal sortation adjustment needs an entry"),
            SeasonalSortationAdjustmentTimezoneLengthRule(64),
        ),
    )
}
