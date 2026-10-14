package com.acme.transport.fleetops.serviceplan.planmetric

/** A rule a global service plan digest must pass. Check returns its problems, or none. */
interface GlobalServicePlanDigestRule {
    fun check(item: GlobalServicePlanDigest): MutableList<String>
}

data class GlobalServicePlanDigestRequiredTimezoneRule(var message: String) : GlobalServicePlanDigestRule {
    override fun check(item: GlobalServicePlanDigest): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalServicePlanDigestScoreRangeRule(
    var min: Double,
    var max: Double,
) : GlobalServicePlanDigestRule {
    override fun check(item: GlobalServicePlanDigest): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class GlobalServicePlanDigestTitleLengthRule(var maxLength: Int) : GlobalServicePlanDigestRule {
    override fun check(item: GlobalServicePlanDigest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalServicePlanDigestCodeLengthRule(var maxLength: Int) : GlobalServicePlanDigestRule {
    override fun check(item: GlobalServicePlanDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalServicePlanDigestEndsAtRangeRule(var min: Int, var max: Int) : GlobalServicePlanDigestRule {
    override fun check(item: GlobalServicePlanDigest): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a global service plan digest. */
data class GlobalServicePlanDigestValidator(var rules: MutableList<GlobalServicePlanDigestRule>) {
    fun validate(item: GlobalServicePlanDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalServicePlanDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalServicePlanDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalServicePlanDigestValidator(): GlobalServicePlanDigestValidator {
    return GlobalServicePlanDigestValidator(
        mutableListOf<GlobalServicePlanDigestRule>(
            GlobalServicePlanDigestRequiredTimezoneRule("global service plan digest needs a value"),
            GlobalServicePlanDigestScoreRangeRule(7.0, 579.8),
            GlobalServicePlanDigestTitleLengthRule(3600),
            GlobalServicePlanDigestCodeLengthRule(120),
            GlobalServicePlanDigestEndsAtRangeRule(64, 1000),
        ),
    )
}
