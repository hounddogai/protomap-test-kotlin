package com.acme.transport.emissions.fuelburn.burnmilestone

import com.acme.transport.kit.text.normalize

/** A rule a tiered fuel burn adjustment must pass. Check returns its problems, or none. */
interface TieredFuelBurnAdjustmentRule {
    fun check(item: TieredFuelBurnAdjustment): MutableList<String>
}

data class TieredFuelBurnAdjustmentContactEmailLengthRule(var maxLength: Int) : TieredFuelBurnAdjustmentRule {
    override fun check(item: TieredFuelBurnAdjustment): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelBurnAdjustmentPinnedGuardRule(var limit: Int) : TieredFuelBurnAdjustmentRule {
    override fun check(item: TieredFuelBurnAdjustment): MutableList<String> {
        if (item.pinned && item.attempts > limit) {
            return mutableListOf<String>("attempts ${item.attempts} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelBurnAdjustmentAllowedLabelRule(
    var allowed: MutableList<String>,
) : TieredFuelBurnAdjustmentRule {
    override fun check(item: TieredFuelBurnAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered fuel burn adjustment. */
data class TieredFuelBurnAdjustmentValidator(var rules: MutableList<TieredFuelBurnAdjustmentRule>) {
    fun validate(item: TieredFuelBurnAdjustment): MutableList<String> {
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

    fun requireValid(item: TieredFuelBurnAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredFuelBurnAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredFuelBurnAdjustmentValidator(): TieredFuelBurnAdjustmentValidator {
    return TieredFuelBurnAdjustmentValidator(
        mutableListOf<TieredFuelBurnAdjustmentRule>(
            TieredFuelBurnAdjustmentContactEmailLengthRule(32),
            TieredFuelBurnAdjustmentPinnedGuardRule(32),
            TieredFuelBurnAdjustmentAllowedLabelRule(mutableListOf<String>("lenient")),
        ),
    )
}
