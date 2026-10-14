package com.acme.transport.linehaul.backhaul.rollup

/** A rule a fallback backhaul milestone must pass. Check returns its problems, or none. */
interface FallbackBackhaulMilestoneRule {
    fun check(item: FallbackBackhaulMilestone): MutableList<String>
}

data class FallbackBackhaulMilestoneCodePrefixRule(var prefix: String) : FallbackBackhaulMilestoneRule {
    override fun check(item: FallbackBackhaulMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulMilestoneRequiredVehicleIdRule(var message: String) : FallbackBackhaulMilestoneRule {
    override fun check(item: FallbackBackhaulMilestone): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulMilestoneVehicleIdLengthRule(var maxLength: Int) : FallbackBackhaulMilestoneRule {
    override fun check(item: FallbackBackhaulMilestone): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulMilestoneCostRangeRule(
    var min: Double,
    var max: Double,
) : FallbackBackhaulMilestoneRule {
    override fun check(item: FallbackBackhaulMilestone): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback backhaul milestone. */
data class FallbackBackhaulMilestoneValidator(var rules: MutableList<FallbackBackhaulMilestoneRule>) {
    fun validate(item: FallbackBackhaulMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackBackhaulMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackBackhaulMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackBackhaulMilestoneValidator(): FallbackBackhaulMilestoneValidator {
    return FallbackBackhaulMilestoneValidator(
        mutableListOf<FallbackBackhaulMilestoneRule>(
            FallbackBackhaulMilestoneCodePrefixRule("REF"),
            FallbackBackhaulMilestoneRequiredVehicleIdRule("fallback backhaul milestone needs an entry"),
            FallbackBackhaulMilestoneVehicleIdLengthRule(255),
            FallbackBackhaulMilestoneCostRangeRule(2.8, 214.2),
        ),
    )
}
