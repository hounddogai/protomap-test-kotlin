package com.acme.transport.freightpay.ratecard.rule

/** A rule a rate card rule must pass. Check returns its problems, or none. */
interface RateCardRuleRule {
    fun check(item: RateCardRule): MutableList<String>
}

data class RateCardRuleRequiredLabelRule(var message: String) : RateCardRuleRule {
    override fun check(item: RateCardRule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardRuleLabelLengthRule(var maxLength: Int) : RateCardRuleRule {
    override fun check(item: RateCardRule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardRuleRequiredCodeRule(var message: String) : RateCardRuleRule {
    override fun check(item: RateCardRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardRuleRequiredDepotRule(var message: String) : RateCardRuleRule {
    override fun check(item: RateCardRule): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardRuleDockLengthRule(var maxLength: Int) : RateCardRuleRule {
    override fun check(item: RateCardRule): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card rule. */
data class RateCardRuleValidator(var rules: MutableList<RateCardRuleRule>) {
    fun validate(item: RateCardRule): MutableList<String> {
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

    fun requireValid(item: RateCardRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardRule>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardRuleValidator(): RateCardRuleValidator {
    return RateCardRuleValidator(
        mutableListOf<RateCardRuleRule>(
            RateCardRuleRequiredLabelRule("rate card rule needs this field"),
            RateCardRuleLabelLengthRule(16),
            RateCardRuleRequiredCodeRule("rate card rule needs this field"),
            RateCardRuleRequiredDepotRule("rate card rule needs a value"),
            RateCardRuleDockLengthRule(255),
        ),
    )
}
