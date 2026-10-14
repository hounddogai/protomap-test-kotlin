package com.acme.transport.linehaul.backhaul.rollup

/** A rule a legacy backhaul rule must pass. Check returns its problems, or none. */
interface LegacyBackhaulRuleRule {
    fun check(item: LegacyBackhaulRule): MutableList<String>
}

data class LegacyBackhaulRuleRequiredLabelRule(var message: String) : LegacyBackhaulRuleRule {
    override fun check(item: LegacyBackhaulRule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyBackhaulRuleLocaleLengthRule(var maxLength: Int) : LegacyBackhaulRuleRule {
    override fun check(item: LegacyBackhaulRule): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyBackhaulRuleWindowDaysRangeRule(var min: Int, var max: Int) : LegacyBackhaulRuleRule {
    override fun check(item: LegacyBackhaulRule): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class LegacyBackhaulRuleAmountRangeRule(var min: Double, var max: Double) : LegacyBackhaulRuleRule {
    override fun check(item: LegacyBackhaulRule): MutableList<String> {
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

data class LegacyBackhaulRuleRequiredLocaleRule(var message: String) : LegacyBackhaulRuleRule {
    override fun check(item: LegacyBackhaulRule): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy backhaul rule. */
data class LegacyBackhaulRuleValidator(var rules: MutableList<LegacyBackhaulRuleRule>) {
    fun validate(item: LegacyBackhaulRule): MutableList<String> {
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

    fun requireValid(item: LegacyBackhaulRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyBackhaulRule>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyBackhaulRuleValidator(): LegacyBackhaulRuleValidator {
    return LegacyBackhaulRuleValidator(
        mutableListOf<LegacyBackhaulRuleRule>(
            LegacyBackhaulRuleRequiredLabelRule("legacy backhaul rule needs this field"),
            LegacyBackhaulRuleLocaleLengthRule(255),
            LegacyBackhaulRuleWindowDaysRangeRule(16, 120),
            LegacyBackhaulRuleAmountRangeRule(2.4, 4932.6),
            LegacyBackhaulRuleRequiredLocaleRule("legacy backhaul rule needs an entry"),
        ),
    )
}
