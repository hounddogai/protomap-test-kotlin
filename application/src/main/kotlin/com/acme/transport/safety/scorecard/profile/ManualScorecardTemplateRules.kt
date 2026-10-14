package com.acme.transport.safety.scorecard.profile

/** A rule a manual scorecard template must pass. Check returns its problems, or none. */
interface ManualScorecardTemplateRule {
    fun check(item: ManualScorecardTemplate): MutableList<String>
}

data class ManualScorecardTemplateDepotLengthRule(var maxLength: Int) : ManualScorecardTemplateRule {
    override fun check(item: ManualScorecardTemplate): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualScorecardTemplateRequiredTitleRule(var message: String) : ManualScorecardTemplateRule {
    override fun check(item: ManualScorecardTemplate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualScorecardTemplateRequiredCodeRule(var message: String) : ManualScorecardTemplateRule {
    override fun check(item: ManualScorecardTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualScorecardTemplateRequiredDepotRule(var message: String) : ManualScorecardTemplateRule {
    override fun check(item: ManualScorecardTemplate): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual scorecard template. */
data class ManualScorecardTemplateValidator(var rules: MutableList<ManualScorecardTemplateRule>) {
    fun validate(item: ManualScorecardTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualScorecardTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualScorecardTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultManualScorecardTemplateValidator(): ManualScorecardTemplateValidator {
    return ManualScorecardTemplateValidator(
        mutableListOf<ManualScorecardTemplateRule>(
            ManualScorecardTemplateDepotLengthRule(1000),
            ManualScorecardTemplateRequiredTitleRule("manual scorecard template needs a value"),
            ManualScorecardTemplateRequiredCodeRule("manual scorecard template needs a value"),
            ManualScorecardTemplateRequiredDepotRule("manual scorecard template needs this field"),
        ),
    )
}
