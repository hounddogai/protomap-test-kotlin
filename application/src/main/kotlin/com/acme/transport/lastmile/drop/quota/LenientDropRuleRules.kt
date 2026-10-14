package com.acme.transport.lastmile.drop.quota

/** A rule a lenient drop rule must pass. Check returns its problems, or none. */
interface LenientDropRuleRule {
    fun check(item: LenientDropRule): MutableList<String>
}

data class LenientDropRuleCodePrefixRule(var prefix: String) : LenientDropRuleRule {
    override fun check(item: LenientDropRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientDropRuleFloorValueRangeRule(var min: Double, var max: Double) : LenientDropRuleRule {
    override fun check(item: LenientDropRule): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class LenientDropRuleDepotLengthRule(var maxLength: Int) : LenientDropRuleRule {
    override fun check(item: LenientDropRule): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDropRuleRequiredLabelRule(var message: String) : LenientDropRuleRule {
    override fun check(item: LenientDropRule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDropRuleRequiredDepotRule(var message: String) : LenientDropRuleRule {
    override fun check(item: LenientDropRule): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient drop rule. */
data class LenientDropRuleValidator(var rules: MutableList<LenientDropRuleRule>) {
    fun validate(item: LenientDropRule): MutableList<String> {
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

    fun requireValid(item: LenientDropRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDropRule>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDropRuleValidator(): LenientDropRuleValidator {
    return LenientDropRuleValidator(
        mutableListOf<LenientDropRuleRule>(
            LenientDropRuleCodePrefixRule("X"),
            LenientDropRuleFloorValueRangeRule(9.1, 1521.9),
            LenientDropRuleDepotLengthRule(80),
            LenientDropRuleRequiredLabelRule("lenient drop rule needs a value"),
            LenientDropRuleRequiredDepotRule("lenient drop rule needs a value"),
        ),
    )
}
