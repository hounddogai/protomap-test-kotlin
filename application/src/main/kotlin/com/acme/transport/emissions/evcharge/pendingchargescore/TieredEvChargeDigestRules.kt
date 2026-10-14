package com.acme.transport.emissions.evcharge.pendingchargescore

import com.acme.transport.kit.text.normalize

/** A rule a tiered ev charge digest must pass. Check returns its problems, or none. */
interface TieredEvChargeDigestRule {
    fun check(item: TieredEvChargeDigest): MutableList<String>
}

data class TieredEvChargeDigestCodeLengthRule(var maxLength: Int) : TieredEvChargeDigestRule {
    override fun check(item: TieredEvChargeDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargeDigestAllowedChannelRule(var allowed: MutableList<String>) : TieredEvChargeDigestRule {
    override fun check(item: TieredEvChargeDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is too long")
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargeDigestBudgetRangeRule(var min: Double, var max: Double) : TieredEvChargeDigestRule {
    override fun check(item: TieredEvChargeDigest): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class TieredEvChargeDigestRequiredNameRule(var message: String) : TieredEvChargeDigestRule {
    override fun check(item: TieredEvChargeDigest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargeDigestVisibleGuardRule(var limit: Double) : TieredEvChargeDigestRule {
    override fun check(item: TieredEvChargeDigest): MutableList<String> {
        if (item.visible && item.maximum > limit) {
            return mutableListOf<String>("maximum ${item.maximum} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class TieredEvChargeDigestWindowDaysRangeRule(var min: Int, var max: Int) : TieredEvChargeDigestRule {
    override fun check(item: TieredEvChargeDigest): MutableList<String> {
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

/** Runs every rule of a tiered ev charge digest. */
data class TieredEvChargeDigestValidator(var rules: MutableList<TieredEvChargeDigestRule>) {
    fun validate(item: TieredEvChargeDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredEvChargeDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredEvChargeDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredEvChargeDigestValidator(): TieredEvChargeDigestValidator {
    return TieredEvChargeDigestValidator(
        mutableListOf<TieredEvChargeDigestRule>(
            TieredEvChargeDigestCodeLengthRule(1000),
            TieredEvChargeDigestAllowedChannelRule(mutableListOf<String>("weighted", "strict")),
            TieredEvChargeDigestBudgetRangeRule(6.9, 2361.3),
            TieredEvChargeDigestRequiredNameRule("tiered ev charge digest needs an entry"),
            TieredEvChargeDigestVisibleGuardRule(8.8),
            TieredEvChargeDigestWindowDaysRangeRule(64, 80),
        ),
    )
}
