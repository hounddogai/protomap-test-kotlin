package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.kit.text.normalize

/** A rule a regional rest break adjustment must pass. Check returns its problems, or none. */
interface RegionalRestBreakAdjustmentRule {
    fun check(item: RegionalRestBreakAdjustment): MutableList<String>
}

data class RegionalRestBreakAdjustmentRequiredNameRule(var message: String) : RegionalRestBreakAdjustmentRule {
    override fun check(item: RegionalRestBreakAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalRestBreakAdjustmentCodePrefixRule(var prefix: String) : RegionalRestBreakAdjustmentRule {
    override fun check(item: RegionalRestBreakAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRestBreakAdjustmentAllowedNameRule(
    var allowed: MutableList<String>,
) : RegionalRestBreakAdjustmentRule {
    override fun check(item: RegionalRestBreakAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional rest break adjustment. */
data class RegionalRestBreakAdjustmentValidator(var rules: MutableList<RegionalRestBreakAdjustmentRule>) {
    fun validate(item: RegionalRestBreakAdjustment): MutableList<String> {
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

    fun requireValid(item: RegionalRestBreakAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalRestBreakAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalRestBreakAdjustmentValidator(): RegionalRestBreakAdjustmentValidator {
    return RegionalRestBreakAdjustmentValidator(
        mutableListOf<RegionalRestBreakAdjustmentRule>(
            RegionalRestBreakAdjustmentRequiredNameRule("regional rest break adjustment needs this field"),
            RegionalRestBreakAdjustmentCodePrefixRule("REF"),
            RegionalRestBreakAdjustmentAllowedNameRule(mutableListOf<String>("batched", "lenient")),
        ),
    )
}
