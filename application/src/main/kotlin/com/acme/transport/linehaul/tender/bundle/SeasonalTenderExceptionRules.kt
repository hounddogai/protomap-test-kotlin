package com.acme.transport.linehaul.tender.bundle

/** A rule a seasonal tender exception must pass. Check returns its problems, or none. */
interface SeasonalTenderExceptionRule {
    fun check(item: SeasonalTenderException): MutableList<String>
}

data class SeasonalTenderExceptionMinimumRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalTenderExceptionRule {
    override fun check(item: SeasonalTenderException): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTenderExceptionEndsAtRangeRule(var min: Int, var max: Int) : SeasonalTenderExceptionRule {
    override fun check(item: SeasonalTenderException): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class SeasonalTenderExceptionCodeLengthRule(var maxLength: Int) : SeasonalTenderExceptionRule {
    override fun check(item: SeasonalTenderException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal tender exception. */
data class SeasonalTenderExceptionValidator(var rules: MutableList<SeasonalTenderExceptionRule>) {
    fun validate(item: SeasonalTenderException): MutableList<String> {
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

    fun requireValid(item: SeasonalTenderException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTenderException>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTenderExceptionValidator(): SeasonalTenderExceptionValidator {
    return SeasonalTenderExceptionValidator(
        mutableListOf<SeasonalTenderExceptionRule>(
            SeasonalTenderExceptionMinimumRangeRule(4.4, 4052.6),
            SeasonalTenderExceptionEndsAtRangeRule(3600, 3600),
            SeasonalTenderExceptionCodeLengthRule(80),
        ),
    )
}
