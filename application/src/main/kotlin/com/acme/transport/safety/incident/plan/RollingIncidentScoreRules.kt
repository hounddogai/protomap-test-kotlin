package com.acme.transport.safety.incident.plan

/** A rule a rolling incident score must pass. Check returns its problems, or none. */
interface RollingIncidentScoreRule {
    fun check(item: RollingIncidentScore): MutableList<String>
}

data class RollingIncidentScoreEtaMinutesRangeRule(var min: Int, var max: Int) : RollingIncidentScoreRule {
    override fun check(item: RollingIncidentScore): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingIncidentScoreLabelLengthRule(var maxLength: Int) : RollingIncidentScoreRule {
    override fun check(item: RollingIncidentScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingIncidentScoreRequiredRouteCodeRule(var message: String) : RollingIncidentScoreRule {
    override fun check(item: RollingIncidentScore): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingIncidentScoreCategoryLengthRule(var maxLength: Int) : RollingIncidentScoreRule {
    override fun check(item: RollingIncidentScore): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingIncidentScorePriorityRangeRule(var min: Int, var max: Int) : RollingIncidentScoreRule {
    override fun check(item: RollingIncidentScore): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling incident score. */
data class RollingIncidentScoreValidator(var rules: MutableList<RollingIncidentScoreRule>) {
    fun validate(item: RollingIncidentScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingIncidentScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingIncidentScore>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingIncidentScoreValidator(): RollingIncidentScoreValidator {
    return RollingIncidentScoreValidator(
        mutableListOf<RollingIncidentScoreRule>(
            RollingIncidentScoreEtaMinutesRangeRule(64, 1000),
            RollingIncidentScoreLabelLengthRule(32),
            RollingIncidentScoreRequiredRouteCodeRule("rolling incident score needs this field"),
            RollingIncidentScoreCategoryLengthRule(3600),
            RollingIncidentScorePriorityRangeRule(32, 32),
        ),
    )
}
