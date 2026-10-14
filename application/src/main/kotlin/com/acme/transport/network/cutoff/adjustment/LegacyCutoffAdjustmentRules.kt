package com.acme.transport.network.cutoff.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a legacy cutoff adjustment must pass. Check returns its problems, or none. */
interface LegacyCutoffAdjustmentRule {
    fun check(item: LegacyCutoffAdjustment): MutableList<String>
}

data class LegacyCutoffAdjustmentDepotLengthRule(var maxLength: Int) : LegacyCutoffAdjustmentRule {
    override fun check(item: LegacyCutoffAdjustment): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCutoffAdjustmentCodeLengthRule(var maxLength: Int) : LegacyCutoffAdjustmentRule {
    override fun check(item: LegacyCutoffAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCutoffAdjustmentRequiredDepotRule(var message: String) : LegacyCutoffAdjustmentRule {
    override fun check(item: LegacyCutoffAdjustment): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyCutoffAdjustmentCodePrefixRule(var prefix: String) : LegacyCutoffAdjustmentRule {
    override fun check(item: LegacyCutoffAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCutoffAdjustmentTitleLengthRule(var maxLength: Int) : LegacyCutoffAdjustmentRule {
    override fun check(item: LegacyCutoffAdjustment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCutoffAdjustmentAllowedCodeRule(
    var allowed: MutableList<String>,
) : LegacyCutoffAdjustmentRule {
    override fun check(item: LegacyCutoffAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy cutoff adjustment. */
data class LegacyCutoffAdjustmentValidator(var rules: MutableList<LegacyCutoffAdjustmentRule>) {
    fun validate(item: LegacyCutoffAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyCutoffAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyCutoffAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyCutoffAdjustmentValidator(): LegacyCutoffAdjustmentValidator {
    return LegacyCutoffAdjustmentValidator(
        mutableListOf<LegacyCutoffAdjustmentRule>(
            LegacyCutoffAdjustmentDepotLengthRule(64),
            LegacyCutoffAdjustmentCodeLengthRule(255),
            LegacyCutoffAdjustmentRequiredDepotRule("legacy cutoff adjustment needs a value"),
            LegacyCutoffAdjustmentCodePrefixRule("Q"),
            LegacyCutoffAdjustmentTitleLengthRule(120),
            LegacyCutoffAdjustmentAllowedCodeRule(mutableListOf<String>("fallback", "rolling")),
        ),
    )
}
