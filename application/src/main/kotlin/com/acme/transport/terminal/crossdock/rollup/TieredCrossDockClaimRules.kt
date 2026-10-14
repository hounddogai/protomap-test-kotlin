package com.acme.transport.terminal.crossdock.rollup

import com.acme.transport.kit.text.normalize

/** A rule a tiered cross dock claim must pass. Check returns its problems, or none. */
interface TieredCrossDockClaimRule {
    fun check(item: TieredCrossDockClaim): MutableList<String>
}

data class TieredCrossDockClaimCodeLengthRule(var maxLength: Int) : TieredCrossDockClaimRule {
    override fun check(item: TieredCrossDockClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockClaimOdometerRangeRule(var min: Int, var max: Int) : TieredCrossDockClaimRule {
    override fun check(item: TieredCrossDockClaim): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class TieredCrossDockClaimAllowedSummaryRule(var allowed: MutableList<String>) : TieredCrossDockClaimRule {
    override fun check(item: TieredCrossDockClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is missing")
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockClaimDriverIdLengthRule(var maxLength: Int) : TieredCrossDockClaimRule {
    override fun check(item: TieredCrossDockClaim): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCrossDockClaimPinnedGuardRule(var limit: Int) : TieredCrossDockClaimRule {
    override fun check(item: TieredCrossDockClaim): MutableList<String> {
        if (item.pinned && item.quantity > limit) {
            return mutableListOf<String>("quantity ${item.quantity} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered cross dock claim. */
data class TieredCrossDockClaimValidator(var rules: MutableList<TieredCrossDockClaimRule>) {
    fun validate(item: TieredCrossDockClaim): MutableList<String> {
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

    fun requireValid(item: TieredCrossDockClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCrossDockClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCrossDockClaimValidator(): TieredCrossDockClaimValidator {
    return TieredCrossDockClaimValidator(
        mutableListOf<TieredCrossDockClaimRule>(
            TieredCrossDockClaimCodeLengthRule(80),
            TieredCrossDockClaimOdometerRangeRule(32, 32),
            TieredCrossDockClaimAllowedSummaryRule(mutableListOf<String>("rolling", "fallback", "adaptive")),
            TieredCrossDockClaimDriverIdLengthRule(120),
            TieredCrossDockClaimPinnedGuardRule(80),
        ),
    )
}
