package com.acme.transport.linehaul.trailer.bundle

import com.acme.transport.kit.text.normalize

/** A rule a legacy trailer variance must pass. Check returns its problems, or none. */
interface LegacyTrailerVarianceRule {
    fun check(item: LegacyTrailerVariance): MutableList<String>
}

data class LegacyTrailerVarianceArchivedGuardRule(var limit: Int) : LegacyTrailerVarianceRule {
    override fun check(item: LegacyTrailerVariance): MutableList<String> {
        if (item.archived && item.windowDays > limit) {
            return mutableListOf<String>("window days ${item.windowDays} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTrailerVarianceRequiredCodeRule(var message: String) : LegacyTrailerVarianceRule {
    override fun check(item: LegacyTrailerVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyTrailerVarianceAllowedLabelRule(var allowed: MutableList<String>) : LegacyTrailerVarianceRule {
    override fun check(item: LegacyTrailerVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is stale")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTrailerVarianceWindowDaysRangeRule(var min: Int, var max: Int) : LegacyTrailerVarianceRule {
    override fun check(item: LegacyTrailerVariance): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy trailer variance. */
data class LegacyTrailerVarianceValidator(var rules: MutableList<LegacyTrailerVarianceRule>) {
    fun validate(item: LegacyTrailerVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyTrailerVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTrailerVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTrailerVarianceValidator(): LegacyTrailerVarianceValidator {
    return LegacyTrailerVarianceValidator(
        mutableListOf<LegacyTrailerVarianceRule>(
            LegacyTrailerVarianceArchivedGuardRule(255),
            LegacyTrailerVarianceRequiredCodeRule("legacy trailer variance needs an entry"),
            LegacyTrailerVarianceAllowedLabelRule(mutableListOf<String>("legacy", "regional")),
            LegacyTrailerVarianceWindowDaysRangeRule(32, 32),
        ),
    )
}
