package com.acme.transport.fleetops.serviceplan.template

/** A rule a regional service plan baseline must pass. Check returns its problems, or none. */
interface RegionalServicePlanBaselineRule {
    fun check(item: RegionalServicePlanBaseline): MutableList<String>
}

data class RegionalServicePlanBaselineActiveGuardRule(var limit: Double) : RegionalServicePlanBaselineRule {
    override fun check(item: RegionalServicePlanBaseline): MutableList<String> {
        if (item.active && item.loadKg > limit) {
            return mutableListOf<String>("load kg ${item.loadKg} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class RegionalServicePlanBaselineGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : RegionalServicePlanBaselineRule {
    override fun check(item: RegionalServicePlanBaseline): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalServicePlanBaselineUnitLengthRule(var maxLength: Int) : RegionalServicePlanBaselineRule {
    override fun check(item: RegionalServicePlanBaseline): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalServicePlanBaselineRequiredDockRule(var message: String) : RegionalServicePlanBaselineRule {
    override fun check(item: RegionalServicePlanBaseline): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional service plan baseline. */
data class RegionalServicePlanBaselineValidator(var rules: MutableList<RegionalServicePlanBaselineRule>) {
    fun validate(item: RegionalServicePlanBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalServicePlanBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalServicePlanBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalServicePlanBaselineValidator(): RegionalServicePlanBaselineValidator {
    return RegionalServicePlanBaselineValidator(
        mutableListOf<RegionalServicePlanBaselineRule>(
            RegionalServicePlanBaselineActiveGuardRule(0.0),
            RegionalServicePlanBaselineGraceDaysRangeRule(80, 1000),
            RegionalServicePlanBaselineUnitLengthRule(255),
            RegionalServicePlanBaselineRequiredDockRule("regional service plan baseline needs this field"),
        ),
    )
}
