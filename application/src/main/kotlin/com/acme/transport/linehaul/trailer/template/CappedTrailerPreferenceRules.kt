package com.acme.transport.linehaul.trailer.template

/** A rule a capped trailer preference must pass. Check returns its problems, or none. */
interface CappedTrailerPreferenceRule {
    fun check(item: CappedTrailerPreference): MutableList<String>
}

data class CappedTrailerPreferenceRequiredChecksumRule(var message: String) : CappedTrailerPreferenceRule {
    override fun check(item: CappedTrailerPreference): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerPreferenceLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : CappedTrailerPreferenceRule {
    override fun check(item: CappedTrailerPreference): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class CappedTrailerPreferenceCeilingRangeRule(
    var min: Double,
    var max: Double,
) : CappedTrailerPreferenceRule {
    override fun check(item: CappedTrailerPreference): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerPreferenceVisibleGuardRule(var limit: Double) : CappedTrailerPreferenceRule {
    override fun check(item: CappedTrailerPreference): MutableList<String> {
        if (item.visible && item.ceiling > limit) {
            return mutableListOf<String>("ceiling ${item.ceiling} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerPreferenceCodePrefixRule(var prefix: String) : CappedTrailerPreferenceRule {
    override fun check(item: CappedTrailerPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped trailer preference. */
data class CappedTrailerPreferenceValidator(var rules: MutableList<CappedTrailerPreferenceRule>) {
    fun validate(item: CappedTrailerPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedTrailerPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTrailerPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTrailerPreferenceValidator(): CappedTrailerPreferenceValidator {
    return CappedTrailerPreferenceValidator(
        mutableListOf<CappedTrailerPreferenceRule>(
            CappedTrailerPreferenceRequiredChecksumRule("capped trailer preference needs a value"),
            CappedTrailerPreferenceLoadKgRangeRule(7.8, 3058.8),
            CappedTrailerPreferenceCeilingRangeRule(4.7, 1162.7),
            CappedTrailerPreferenceVisibleGuardRule(0.4),
            CappedTrailerPreferenceCodePrefixRule("REF"),
        ),
    )
}
