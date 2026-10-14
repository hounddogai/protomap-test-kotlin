package com.acme.transport.linehaul.tender.metric

/** A rule a manual tender rule must pass. Check returns its problems, or none. */
interface ManualTenderRuleRule {
    fun check(item: ManualTenderRule): MutableList<String>
}

data class ManualTenderRuleWeightRangeRule(var min: Double, var max: Double) : ManualTenderRuleRule {
    override fun check(item: ManualTenderRule): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualTenderRuleActiveGuardRule(var limit: Double) : ManualTenderRuleRule {
    override fun check(item: ManualTenderRule): MutableList<String> {
        if (item.active && item.weight > limit) {
            return mutableListOf<String>("weight ${item.weight} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class ManualTenderRuleRequiredCodeRule(var message: String) : ManualTenderRuleRule {
    override fun check(item: ManualTenderRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTenderRuleAttemptsRangeRule(var min: Int, var max: Int) : ManualTenderRuleRule {
    override fun check(item: ManualTenderRule): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual tender rule. */
data class ManualTenderRuleValidator(var rules: MutableList<ManualTenderRuleRule>) {
    fun validate(item: ManualTenderRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualTenderRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTenderRule>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTenderRuleValidator(): ManualTenderRuleValidator {
    return ManualTenderRuleValidator(
        mutableListOf<ManualTenderRuleRule>(
            ManualTenderRuleWeightRangeRule(2.6, 4087.5),
            ManualTenderRuleActiveGuardRule(7.5),
            ManualTenderRuleRequiredCodeRule("manual tender rule needs this field"),
            ManualTenderRuleAttemptsRangeRule(3600, 86400),
        ),
    )
}
