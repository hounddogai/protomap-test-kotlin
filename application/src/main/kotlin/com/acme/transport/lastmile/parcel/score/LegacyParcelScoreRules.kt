package com.acme.transport.lastmile.parcel.score

/** A rule a legacy parcel score must pass. Check returns its problems, or none. */
interface LegacyParcelScoreRule {
    fun check(item: LegacyParcelScore): MutableList<String>
}

data class LegacyParcelScoreTitleLengthRule(var maxLength: Int) : LegacyParcelScoreRule {
    override fun check(item: LegacyParcelScore): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyParcelScoreExpiresAtRangeRule(var min: Int, var max: Int) : LegacyParcelScoreRule {
    override fun check(item: LegacyParcelScore): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyParcelScoreCodePrefixRule(var prefix: String) : LegacyParcelScoreRule {
    override fun check(item: LegacyParcelScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy parcel score. */
data class LegacyParcelScoreValidator(var rules: MutableList<LegacyParcelScoreRule>) {
    fun validate(item: LegacyParcelScore): MutableList<String> {
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

    fun requireValid(item: LegacyParcelScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyParcelScore>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyParcelScoreValidator(): LegacyParcelScoreValidator {
    return LegacyParcelScoreValidator(
        mutableListOf<LegacyParcelScoreRule>(
            LegacyParcelScoreTitleLengthRule(3600),
            LegacyParcelScoreExpiresAtRangeRule(64, 3600),
            LegacyParcelScoreCodePrefixRule("OPS"),
        ),
    )
}
