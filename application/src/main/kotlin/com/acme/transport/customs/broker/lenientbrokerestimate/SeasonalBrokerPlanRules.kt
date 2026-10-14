package com.acme.transport.customs.broker.lenientbrokerestimate

/** A rule a seasonal broker plan must pass. Check returns its problems, or none. */
interface SeasonalBrokerPlanRule {
    fun check(item: SeasonalBrokerPlan): MutableList<String>
}

data class SeasonalBrokerPlanRequiredNameRule(var message: String) : SeasonalBrokerPlanRule {
    override fun check(item: SeasonalBrokerPlan): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalBrokerPlanRequiredCodeRule(var message: String) : SeasonalBrokerPlanRule {
    override fun check(item: SeasonalBrokerPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalBrokerPlanCodeLengthRule(var maxLength: Int) : SeasonalBrokerPlanRule {
    override fun check(item: SeasonalBrokerPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalBrokerPlanArchivedGuardRule(var limit: Double) : SeasonalBrokerPlanRule {
    override fun check(item: SeasonalBrokerPlan): MutableList<String> {
        if (item.archived && item.cost > limit) {
            return mutableListOf<String>("cost ${item.cost} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal broker plan. */
data class SeasonalBrokerPlanValidator(var rules: MutableList<SeasonalBrokerPlanRule>) {
    fun validate(item: SeasonalBrokerPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalBrokerPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalBrokerPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalBrokerPlanValidator(): SeasonalBrokerPlanValidator {
    return SeasonalBrokerPlanValidator(
        mutableListOf<SeasonalBrokerPlanRule>(
            SeasonalBrokerPlanRequiredNameRule("seasonal broker plan needs a value"),
            SeasonalBrokerPlanRequiredCodeRule("seasonal broker plan needs a value"),
            SeasonalBrokerPlanCodeLengthRule(3600),
            SeasonalBrokerPlanArchivedGuardRule(4.3),
        ),
    )
}
