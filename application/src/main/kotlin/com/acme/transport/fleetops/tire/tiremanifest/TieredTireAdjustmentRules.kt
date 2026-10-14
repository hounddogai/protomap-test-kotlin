package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.kit.text.normalize

/** A rule a tiered tire adjustment must pass. Check returns its problems, or none. */
interface TieredTireAdjustmentRule {
    fun check(item: TieredTireAdjustment): MutableList<String>
}

data class TieredTireAdjustmentRequiredCodeRule(var message: String) : TieredTireAdjustmentRule {
    override fun check(item: TieredTireAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTireAdjustmentRequiredReviewerRule(var message: String) : TieredTireAdjustmentRule {
    override fun check(item: TieredTireAdjustment): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTireAdjustmentCodeLengthRule(var maxLength: Int) : TieredTireAdjustmentRule {
    override fun check(item: TieredTireAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredTireAdjustmentStartsAtRangeRule(var min: Int, var max: Int) : TieredTireAdjustmentRule {
    override fun check(item: TieredTireAdjustment): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredTireAdjustmentCodePrefixRule(var prefix: String) : TieredTireAdjustmentRule {
    override fun check(item: TieredTireAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredTireAdjustmentAllowedCodeRule(var allowed: MutableList<String>) : TieredTireAdjustmentRule {
    override fun check(item: TieredTireAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered tire adjustment. */
data class TieredTireAdjustmentValidator(var rules: MutableList<TieredTireAdjustmentRule>) {
    fun validate(item: TieredTireAdjustment): MutableList<String> {
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

    fun requireValid(item: TieredTireAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredTireAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredTireAdjustmentValidator(): TieredTireAdjustmentValidator {
    return TieredTireAdjustmentValidator(
        mutableListOf<TieredTireAdjustmentRule>(
            TieredTireAdjustmentRequiredCodeRule("tiered tire adjustment needs an entry"),
            TieredTireAdjustmentRequiredReviewerRule("tiered tire adjustment needs a value"),
            TieredTireAdjustmentCodeLengthRule(32),
            TieredTireAdjustmentStartsAtRangeRule(255, 1000),
            TieredTireAdjustmentCodePrefixRule("X"),
            TieredTireAdjustmentAllowedCodeRule(mutableListOf<String>("seasonal", "strict")),
        ),
    )
}
