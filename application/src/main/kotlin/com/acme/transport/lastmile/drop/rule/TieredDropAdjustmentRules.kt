package com.acme.transport.lastmile.drop.rule

import com.acme.transport.kit.text.normalize

/** A rule a tiered drop adjustment must pass. Check returns its problems, or none. */
interface TieredDropAdjustmentRule {
    fun check(item: TieredDropAdjustment): MutableList<String>
}

data class TieredDropAdjustmentRequiredCodeRule(var message: String) : TieredDropAdjustmentRule {
    override fun check(item: TieredDropAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDropAdjustmentNameLengthRule(var maxLength: Int) : TieredDropAdjustmentRule {
    override fun check(item: TieredDropAdjustment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredDropAdjustmentCodePrefixRule(var prefix: String) : TieredDropAdjustmentRule {
    override fun check(item: TieredDropAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredDropAdjustmentAllowedDescriptionRule(
    var allowed: MutableList<String>,
) : TieredDropAdjustmentRule {
    override fun check(item: TieredDropAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered drop adjustment. */
data class TieredDropAdjustmentValidator(var rules: MutableList<TieredDropAdjustmentRule>) {
    fun validate(item: TieredDropAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredDropAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDropAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDropAdjustmentValidator(): TieredDropAdjustmentValidator {
    return TieredDropAdjustmentValidator(
        mutableListOf<TieredDropAdjustmentRule>(
            TieredDropAdjustmentRequiredCodeRule("tiered drop adjustment needs this field"),
            TieredDropAdjustmentNameLengthRule(1000),
            TieredDropAdjustmentCodePrefixRule("REF"),
            TieredDropAdjustmentAllowedDescriptionRule(mutableListOf<String>("seasonal", "strict")),
        ),
    )
}
