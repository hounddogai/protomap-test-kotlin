package com.acme.transport.safety.nearmiss.variance

import com.acme.transport.kit.text.normalize

/** A rule a tiered near miss adjustment must pass. Check returns its problems, or none. */
interface TieredNearMissAdjustmentRule {
    fun check(item: TieredNearMissAdjustment): MutableList<String>
}

data class TieredNearMissAdjustmentCodePrefixRule(var prefix: String) : TieredNearMissAdjustmentRule {
    override fun check(item: TieredNearMissAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredNearMissAdjustmentAllowedNameRule(
    var allowed: MutableList<String>,
) : TieredNearMissAdjustmentRule {
    override fun check(item: TieredNearMissAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is expired")
        }
        return mutableListOf<String>()
    }
}

data class TieredNearMissAdjustmentRequiredCodeRule(var message: String) : TieredNearMissAdjustmentRule {
    override fun check(item: TieredNearMissAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredNearMissAdjustmentRequiredNameRule(var message: String) : TieredNearMissAdjustmentRule {
    override fun check(item: TieredNearMissAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredNearMissAdjustmentCodeLengthRule(var maxLength: Int) : TieredNearMissAdjustmentRule {
    override fun check(item: TieredNearMissAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered near miss adjustment. */
data class TieredNearMissAdjustmentValidator(var rules: MutableList<TieredNearMissAdjustmentRule>) {
    fun validate(item: TieredNearMissAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredNearMissAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredNearMissAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredNearMissAdjustmentValidator(): TieredNearMissAdjustmentValidator {
    return TieredNearMissAdjustmentValidator(
        mutableListOf<TieredNearMissAdjustmentRule>(
            TieredNearMissAdjustmentCodePrefixRule("X"),
            TieredNearMissAdjustmentAllowedNameRule(mutableListOf<String>("legacy", "default", "default")),
            TieredNearMissAdjustmentRequiredCodeRule("tiered near miss adjustment needs an entry"),
            TieredNearMissAdjustmentRequiredNameRule("tiered near miss adjustment needs a value"),
            TieredNearMissAdjustmentCodeLengthRule(64),
        ),
    )
}
