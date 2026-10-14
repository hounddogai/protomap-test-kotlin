package com.acme.transport.safety.scorecard.scorecardthreshold

/** A rule a primary scorecard adjustment must pass. Check returns its problems, or none. */
interface PrimaryScorecardAdjustmentRule {
    fun check(item: PrimaryScorecardAdjustment): MutableList<String>
}

data class PrimaryScorecardAdjustmentExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryScorecardAdjustmentRule {
    override fun check(item: PrimaryScorecardAdjustment): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class PrimaryScorecardAdjustmentRouteCodeLengthRule(var maxLength: Int) : PrimaryScorecardAdjustmentRule {
    override fun check(item: PrimaryScorecardAdjustment): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryScorecardAdjustmentVersionRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryScorecardAdjustmentRule {
    override fun check(item: PrimaryScorecardAdjustment): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryScorecardAdjustmentRequiredRouteCodeRule(
    var message: String,
) : PrimaryScorecardAdjustmentRule {
    override fun check(item: PrimaryScorecardAdjustment): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary scorecard adjustment. */
data class PrimaryScorecardAdjustmentValidator(var rules: MutableList<PrimaryScorecardAdjustmentRule>) {
    fun validate(item: PrimaryScorecardAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryScorecardAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryScorecardAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryScorecardAdjustmentValidator(): PrimaryScorecardAdjustmentValidator {
    return PrimaryScorecardAdjustmentValidator(
        mutableListOf<PrimaryScorecardAdjustmentRule>(
            PrimaryScorecardAdjustmentExpiresAtRangeRule(120, 1000),
            PrimaryScorecardAdjustmentRouteCodeLengthRule(64),
            PrimaryScorecardAdjustmentVersionRangeRule(255, 3600),
            PrimaryScorecardAdjustmentRequiredRouteCodeRule("primary scorecard adjustment needs this field"),
        ),
    )
}
