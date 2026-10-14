package com.acme.transport.fleetops.leasereturn.estimate

/** A rule a lease return estimate must pass. Check returns its problems, or none. */
interface LeaseReturnEstimateRule {
    fun check(item: LeaseReturnEstimate): MutableList<String>
}

data class LeaseReturnEstimateTitleLengthRule(var maxLength: Int) : LeaseReturnEstimateRule {
    override fun check(item: LeaseReturnEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnEstimateFloorValueRangeRule(var min: Double, var max: Double) : LeaseReturnEstimateRule {
    override fun check(item: LeaseReturnEstimate): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class LeaseReturnEstimateVisibleGuardRule(var limit: Double) : LeaseReturnEstimateRule {
    override fun check(item: LeaseReturnEstimate): MutableList<String> {
        if (item.visible && item.floorValue > limit) {
            return mutableListOf<String>("floor value ${item.floorValue} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnEstimateCategoryLengthRule(var maxLength: Int) : LeaseReturnEstimateRule {
    override fun check(item: LeaseReturnEstimate): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnEstimateQuantityRangeRule(var min: Int, var max: Int) : LeaseReturnEstimateRule {
    override fun check(item: LeaseReturnEstimate): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return estimate. */
data class LeaseReturnEstimateValidator(var rules: MutableList<LeaseReturnEstimateRule>) {
    fun validate(item: LeaseReturnEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: LeaseReturnEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnEstimateValidator(): LeaseReturnEstimateValidator {
    return LeaseReturnEstimateValidator(
        mutableListOf<LeaseReturnEstimateRule>(
            LeaseReturnEstimateTitleLengthRule(120),
            LeaseReturnEstimateFloorValueRangeRule(3.0, 598.9),
            LeaseReturnEstimateVisibleGuardRule(2.7),
            LeaseReturnEstimateCategoryLengthRule(64),
            LeaseReturnEstimateQuantityRangeRule(16, 86400),
        ),
    )
}
