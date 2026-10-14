package com.acme.transport.customs.clearance.variance

import com.acme.transport.kit.text.normalize

/** A rule a legacy clearance score must pass. Check returns its problems, or none. */
interface LegacyClearanceScoreRule {
    fun check(item: LegacyClearanceScore): MutableList<String>
}

data class LegacyClearanceScoreRequiredDriverIdRule(var message: String) : LegacyClearanceScoreRule {
    override fun check(item: LegacyClearanceScore): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyClearanceScoreRequiredCodeRule(var message: String) : LegacyClearanceScoreRule {
    override fun check(item: LegacyClearanceScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyClearanceScoreAllowedCodeRule(var allowed: MutableList<String>) : LegacyClearanceScoreRule {
    override fun check(item: LegacyClearanceScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class LegacyClearanceScoreCodeLengthRule(var maxLength: Int) : LegacyClearanceScoreRule {
    override fun check(item: LegacyClearanceScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyClearanceScoreCodePrefixRule(var prefix: String) : LegacyClearanceScoreRule {
    override fun check(item: LegacyClearanceScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyClearanceScoreLabelLengthRule(var maxLength: Int) : LegacyClearanceScoreRule {
    override fun check(item: LegacyClearanceScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy clearance score. */
data class LegacyClearanceScoreValidator(var rules: MutableList<LegacyClearanceScoreRule>) {
    fun validate(item: LegacyClearanceScore): MutableList<String> {
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

    fun requireValid(item: LegacyClearanceScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyClearanceScore>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyClearanceScoreValidator(): LegacyClearanceScoreValidator {
    return LegacyClearanceScoreValidator(
        mutableListOf<LegacyClearanceScoreRule>(
            LegacyClearanceScoreRequiredDriverIdRule("legacy clearance score needs this field"),
            LegacyClearanceScoreRequiredCodeRule("legacy clearance score needs this field"),
            LegacyClearanceScoreAllowedCodeRule(mutableListOf<String>("default", "primary", "legacy")),
            LegacyClearanceScoreCodeLengthRule(3600),
            LegacyClearanceScoreCodePrefixRule("OPS"),
            LegacyClearanceScoreLabelLengthRule(80),
        ),
    )
}
