package com.acme.transport.linehaul.laneplan.rule

/** A rule a weighted lane plan rule must pass. Check returns its problems, or none. */
interface WeightedLanePlanRuleRule {
    fun check(item: WeightedLanePlanRule): MutableList<String>
}

data class WeightedLanePlanRuleRequiredShiftCodeRule(var message: String) : WeightedLanePlanRuleRule {
    override fun check(item: WeightedLanePlanRule): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedLanePlanRuleRequiredCodeRule(var message: String) : WeightedLanePlanRuleRule {
    override fun check(item: WeightedLanePlanRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedLanePlanRuleShiftCodeLengthRule(var maxLength: Int) : WeightedLanePlanRuleRule {
    override fun check(item: WeightedLanePlanRule): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedLanePlanRuleRequiredNameRule(var message: String) : WeightedLanePlanRuleRule {
    override fun check(item: WeightedLanePlanRule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted lane plan rule. */
data class WeightedLanePlanRuleValidator(var rules: MutableList<WeightedLanePlanRuleRule>) {
    fun validate(item: WeightedLanePlanRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedLanePlanRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedLanePlanRule>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedLanePlanRuleValidator(): WeightedLanePlanRuleValidator {
    return WeightedLanePlanRuleValidator(
        mutableListOf<WeightedLanePlanRuleRule>(
            WeightedLanePlanRuleRequiredShiftCodeRule("weighted lane plan rule needs this field"),
            WeightedLanePlanRuleRequiredCodeRule("weighted lane plan rule needs this field"),
            WeightedLanePlanRuleShiftCodeLengthRule(16),
            WeightedLanePlanRuleRequiredNameRule("weighted lane plan rule needs this field"),
        ),
    )
}
