package com.acme.transport.fleetops.tire.tireagreement

import com.acme.transport.kit.text.normalize

/** A rule a tiered tire batch must pass. Check returns its problems, or none. */
interface TieredTireBatchRule {
    fun check(item: TieredTireBatch): MutableList<String>
}

data class TieredTireBatchAmountRangeRule(var min: Double, var max: Double) : TieredTireBatchRule {
    override fun check(item: TieredTireBatch): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class TieredTireBatchTitleLengthRule(var maxLength: Int) : TieredTireBatchRule {
    override fun check(item: TieredTireBatch): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredTireBatchCodeLengthRule(var maxLength: Int) : TieredTireBatchRule {
    override fun check(item: TieredTireBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredTireBatchAllowedTitleRule(var allowed: MutableList<String>) : TieredTireBatchRule {
    override fun check(item: TieredTireBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class TieredTireBatchRequiredDepotRule(var message: String) : TieredTireBatchRule {
    override fun check(item: TieredTireBatch): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTireBatchPinnedGuardRule(var limit: Int) : TieredTireBatchRule {
    override fun check(item: TieredTireBatch): MutableList<String> {
        if (item.pinned && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered tire batch. */
data class TieredTireBatchValidator(var rules: MutableList<TieredTireBatchRule>) {
    fun validate(item: TieredTireBatch): MutableList<String> {
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

    fun requireValid(item: TieredTireBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredTireBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredTireBatchValidator(): TieredTireBatchValidator {
    return TieredTireBatchValidator(
        mutableListOf<TieredTireBatchRule>(
            TieredTireBatchAmountRangeRule(0.6, 3507.0),
            TieredTireBatchTitleLengthRule(64),
            TieredTireBatchCodeLengthRule(255),
            TieredTireBatchAllowedTitleRule(mutableListOf<String>()),
            TieredTireBatchRequiredDepotRule("tiered tire batch needs this field"),
            TieredTireBatchPinnedGuardRule(32),
        ),
    )
}
