package com.acme.transport.linehaul.tender.review

/** A rule a rolling tender preference must pass. Check returns its problems, or none. */
interface RollingTenderPreferenceRule {
    fun check(item: RollingTenderPreference): MutableList<String>
}

data class RollingTenderPreferenceRequiredChecksumRule(var message: String) : RollingTenderPreferenceRule {
    override fun check(item: RollingTenderPreference): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingTenderPreferenceRegionLengthRule(var maxLength: Int) : RollingTenderPreferenceRule {
    override fun check(item: RollingTenderPreference): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingTenderPreferenceScoreRangeRule(
    var min: Double,
    var max: Double,
) : RollingTenderPreferenceRule {
    override fun check(item: RollingTenderPreference): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling tender preference. */
data class RollingTenderPreferenceValidator(var rules: MutableList<RollingTenderPreferenceRule>) {
    fun validate(item: RollingTenderPreference): MutableList<String> {
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

    fun requireValid(item: RollingTenderPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingTenderPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingTenderPreferenceValidator(): RollingTenderPreferenceValidator {
    return RollingTenderPreferenceValidator(
        mutableListOf<RollingTenderPreferenceRule>(
            RollingTenderPreferenceRequiredChecksumRule("rolling tender preference needs this field"),
            RollingTenderPreferenceRegionLengthRule(32),
            RollingTenderPreferenceScoreRangeRule(0.3, 1790.6),
        ),
    )
}
