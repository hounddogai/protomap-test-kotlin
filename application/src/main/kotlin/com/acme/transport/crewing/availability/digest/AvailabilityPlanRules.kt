package com.acme.transport.crewing.availability.digest

/** A rule a availability plan must pass. Check returns its problems, or none. */
interface AvailabilityPlanRule {
    fun check(item: AvailabilityPlan): MutableList<String>
}

data class AvailabilityPlanRequiredTitleRule(var message: String) : AvailabilityPlanRule {
    override fun check(item: AvailabilityPlan): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityPlanTitleLengthRule(var maxLength: Int) : AvailabilityPlanRule {
    override fun check(item: AvailabilityPlan): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityPlanVisibleGuardRule(var limit: Double) : AvailabilityPlanRule {
    override fun check(item: AvailabilityPlan): MutableList<String> {
        if (item.visible && item.fuelLiters > limit) {
            return mutableListOf<String>("fuel liters ${item.fuelLiters} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityPlanCodePrefixRule(var prefix: String) : AvailabilityPlanRule {
    override fun check(item: AvailabilityPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability plan. */
data class AvailabilityPlanValidator(var rules: MutableList<AvailabilityPlanRule>) {
    fun validate(item: AvailabilityPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AvailabilityPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityPlanValidator(): AvailabilityPlanValidator {
    return AvailabilityPlanValidator(
        mutableListOf<AvailabilityPlanRule>(
            AvailabilityPlanRequiredTitleRule("availability plan needs a value"),
            AvailabilityPlanTitleLengthRule(86400),
            AvailabilityPlanVisibleGuardRule(7.1),
            AvailabilityPlanCodePrefixRule("REF"),
        ),
    )
}
