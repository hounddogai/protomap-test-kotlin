package com.acme.transport.emissions.idling.idlingsnapshot

import com.acme.transport.kit.text.normalize

/** A rule a adaptive idling snapshot must pass. Check returns its problems, or none. */
interface AdaptiveIdlingSnapshotRule {
    fun check(item: AdaptiveIdlingSnapshot): MutableList<String>
}

data class AdaptiveIdlingSnapshotRateRangeRule(var min: Double, var max: Double) : AdaptiveIdlingSnapshotRule {
    override fun check(item: AdaptiveIdlingSnapshot): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIdlingSnapshotAllowedCurrencyRule(
    var allowed: MutableList<String>,
) : AdaptiveIdlingSnapshotRule {
    override fun check(item: AdaptiveIdlingSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.currency))) {
            return mutableListOf<String>("currency ${item.currency} is expired")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIdlingSnapshotCurrencyLengthRule(var maxLength: Int) : AdaptiveIdlingSnapshotRule {
    override fun check(item: AdaptiveIdlingSnapshot): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIdlingSnapshotActiveGuardRule(var limit: Int) : AdaptiveIdlingSnapshotRule {
    override fun check(item: AdaptiveIdlingSnapshot): MutableList<String> {
        if (item.active && item.expiresAt > limit) {
            return mutableListOf<String>("expires at ${item.expiresAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIdlingSnapshotExpiresAtRangeRule(var min: Int, var max: Int) : AdaptiveIdlingSnapshotRule {
    override fun check(item: AdaptiveIdlingSnapshot): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIdlingSnapshotRequiredTitleRule(var message: String) : AdaptiveIdlingSnapshotRule {
    override fun check(item: AdaptiveIdlingSnapshot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive idling snapshot. */
data class AdaptiveIdlingSnapshotValidator(var rules: MutableList<AdaptiveIdlingSnapshotRule>) {
    fun validate(item: AdaptiveIdlingSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveIdlingSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveIdlingSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveIdlingSnapshotValidator(): AdaptiveIdlingSnapshotValidator {
    return AdaptiveIdlingSnapshotValidator(
        mutableListOf<AdaptiveIdlingSnapshotRule>(
            AdaptiveIdlingSnapshotRateRangeRule(7.8, 2113.8),
            AdaptiveIdlingSnapshotAllowedCurrencyRule(mutableListOf<String>("global", "fallback")),
            AdaptiveIdlingSnapshotCurrencyLengthRule(32),
            AdaptiveIdlingSnapshotActiveGuardRule(86400),
            AdaptiveIdlingSnapshotExpiresAtRangeRule(80, 80),
            AdaptiveIdlingSnapshotRequiredTitleRule("adaptive idling snapshot needs a value"),
        ),
    )
}
