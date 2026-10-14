package com.acme.transport.linehaul.backhaul.backhaulthreshold

/** A rule a backhaul rule must pass. Check returns its problems, or none. */
interface BackhaulRuleRule {
    fun check(item: BackhaulRule): MutableList<String>
}

data class BackhaulRuleRequiredNameRule(var message: String) : BackhaulRuleRule {
    override fun check(item: BackhaulRule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulRuleCostRangeRule(var min: Double, var max: Double) : BackhaulRuleRule {
    override fun check(item: BackhaulRule): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class BackhaulRuleNameLengthRule(var maxLength: Int) : BackhaulRuleRule {
    override fun check(item: BackhaulRule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul rule. */
data class BackhaulRuleValidator(var rules: MutableList<BackhaulRuleRule>) {
    fun validate(item: BackhaulRule): MutableList<String> {
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

    fun requireValid(item: BackhaulRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulRule>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulRuleValidator(): BackhaulRuleValidator {
    return BackhaulRuleValidator(
        mutableListOf<BackhaulRuleRule>(
            BackhaulRuleRequiredNameRule("backhaul rule needs a value"),
            BackhaulRuleCostRangeRule(6.2, 2381.0),
            BackhaulRuleNameLengthRule(64),
        ),
    )
}
