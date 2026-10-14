package com.acme.transport.fleetops.serviceplan.assignment

/** A rule a primary service plan mapping must pass. Check returns its problems, or none. */
interface PrimaryServicePlanMappingRule {
    fun check(item: PrimaryServicePlanMapping): MutableList<String>
}

data class PrimaryServicePlanMappingArchivedGuardRule(var limit: Int) : PrimaryServicePlanMappingRule {
    override fun check(item: PrimaryServicePlanMapping): MutableList<String> {
        if (item.archived && item.stopCount > limit) {
            return mutableListOf<String>("stop count ${item.stopCount} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServicePlanMappingStopCountRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryServicePlanMappingRule {
    override fun check(item: PrimaryServicePlanMapping): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class PrimaryServicePlanMappingRequiredCodeRule(var message: String) : PrimaryServicePlanMappingRule {
    override fun check(item: PrimaryServicePlanMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServicePlanMappingRequiredTitleRule(var message: String) : PrimaryServicePlanMappingRule {
    override fun check(item: PrimaryServicePlanMapping): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServicePlanMappingMinimumRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryServicePlanMappingRule {
    override fun check(item: PrimaryServicePlanMapping): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary service plan mapping. */
data class PrimaryServicePlanMappingValidator(var rules: MutableList<PrimaryServicePlanMappingRule>) {
    fun validate(item: PrimaryServicePlanMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryServicePlanMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryServicePlanMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryServicePlanMappingValidator(): PrimaryServicePlanMappingValidator {
    return PrimaryServicePlanMappingValidator(
        mutableListOf<PrimaryServicePlanMappingRule>(
            PrimaryServicePlanMappingArchivedGuardRule(255),
            PrimaryServicePlanMappingStopCountRangeRule(1000, 3600),
            PrimaryServicePlanMappingRequiredCodeRule("primary service plan mapping needs a value"),
            PrimaryServicePlanMappingRequiredTitleRule("primary service plan mapping needs a value"),
            PrimaryServicePlanMappingMinimumRangeRule(2.7, 4452.4),
        ),
    )
}
