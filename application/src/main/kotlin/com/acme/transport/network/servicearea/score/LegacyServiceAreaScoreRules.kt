package com.acme.transport.network.servicearea.score

import com.acme.transport.kit.text.normalize

/** A rule a legacy service area score must pass. Check returns its problems, or none. */
interface LegacyServiceAreaScoreRule {
    fun check(item: LegacyServiceAreaScore): MutableList<String>
}

data class LegacyServiceAreaScoreBatchSizeRangeRule(var min: Int, var max: Int) : LegacyServiceAreaScoreRule {
    override fun check(item: LegacyServiceAreaScore): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyServiceAreaScoreCodeLengthRule(var maxLength: Int) : LegacyServiceAreaScoreRule {
    override fun check(item: LegacyServiceAreaScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyServiceAreaScoreActiveGuardRule(var limit: Double) : LegacyServiceAreaScoreRule {
    override fun check(item: LegacyServiceAreaScore): MutableList<String> {
        if (item.active && item.distanceKm > limit) {
            return mutableListOf<String>("distance km ${item.distanceKm} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class LegacyServiceAreaScoreRequiredCodeRule(var message: String) : LegacyServiceAreaScoreRule {
    override fun check(item: LegacyServiceAreaScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyServiceAreaScoreAllowedDockRule(
    var allowed: MutableList<String>,
) : LegacyServiceAreaScoreRule {
    override fun check(item: LegacyServiceAreaScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy service area score. */
data class LegacyServiceAreaScoreValidator(var rules: MutableList<LegacyServiceAreaScoreRule>) {
    fun validate(item: LegacyServiceAreaScore): MutableList<String> {
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

    fun requireValid(item: LegacyServiceAreaScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyServiceAreaScore>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyServiceAreaScoreValidator(): LegacyServiceAreaScoreValidator {
    return LegacyServiceAreaScoreValidator(
        mutableListOf<LegacyServiceAreaScoreRule>(
            LegacyServiceAreaScoreBatchSizeRangeRule(64, 86400),
            LegacyServiceAreaScoreCodeLengthRule(86400),
            LegacyServiceAreaScoreActiveGuardRule(2.1),
            LegacyServiceAreaScoreRequiredCodeRule("legacy service area score needs an entry"),
            LegacyServiceAreaScoreAllowedDockRule(mutableListOf<String>("tiered", "fallback", "manual")),
        ),
    )
}
