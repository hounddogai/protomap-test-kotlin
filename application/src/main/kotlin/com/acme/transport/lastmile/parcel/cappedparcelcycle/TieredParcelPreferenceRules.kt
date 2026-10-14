package com.acme.transport.lastmile.parcel.cappedparcelcycle

/** A rule a tiered parcel preference must pass. Check returns its problems, or none. */
interface TieredParcelPreferenceRule {
    fun check(item: TieredParcelPreference): MutableList<String>
}

data class TieredParcelPreferenceTitleLengthRule(var maxLength: Int) : TieredParcelPreferenceRule {
    override fun check(item: TieredParcelPreference): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredParcelPreferenceScoreRangeRule(var min: Double, var max: Double) : TieredParcelPreferenceRule {
    override fun check(item: TieredParcelPreference): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class TieredParcelPreferenceRequiredTitleRule(var message: String) : TieredParcelPreferenceRule {
    override fun check(item: TieredParcelPreference): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredParcelPreferenceDescriptionLengthRule(var maxLength: Int) : TieredParcelPreferenceRule {
    override fun check(item: TieredParcelPreference): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredParcelPreferenceCodePrefixRule(var prefix: String) : TieredParcelPreferenceRule {
    override fun check(item: TieredParcelPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered parcel preference. */
data class TieredParcelPreferenceValidator(var rules: MutableList<TieredParcelPreferenceRule>) {
    fun validate(item: TieredParcelPreference): MutableList<String> {
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

    fun requireValid(item: TieredParcelPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredParcelPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredParcelPreferenceValidator(): TieredParcelPreferenceValidator {
    return TieredParcelPreferenceValidator(
        mutableListOf<TieredParcelPreferenceRule>(
            TieredParcelPreferenceTitleLengthRule(120),
            TieredParcelPreferenceScoreRangeRule(6.5, 909.9),
            TieredParcelPreferenceRequiredTitleRule("tiered parcel preference needs this field"),
            TieredParcelPreferenceDescriptionLengthRule(86400),
            TieredParcelPreferenceCodePrefixRule("ACM"),
        ),
    )
}
