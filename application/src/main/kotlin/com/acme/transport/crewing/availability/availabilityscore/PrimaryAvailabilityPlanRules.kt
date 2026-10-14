package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.kit.text.normalize

/** A rule a primary availability plan must pass. Check returns its problems, or none. */
interface PrimaryAvailabilityPlanRule {
    fun check(item: PrimaryAvailabilityPlan): MutableList<String>
}

data class PrimaryAvailabilityPlanDiscountRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryAvailabilityPlanRule {
    override fun check(item: PrimaryAvailabilityPlan): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class PrimaryAvailabilityPlanAllowedDockRule(
    var allowed: MutableList<String>,
) : PrimaryAvailabilityPlanRule {
    override fun check(item: PrimaryAvailabilityPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAvailabilityPlanOwnerIdLengthRule(var maxLength: Int) : PrimaryAvailabilityPlanRule {
    override fun check(item: PrimaryAvailabilityPlan): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAvailabilityPlanDockLengthRule(var maxLength: Int) : PrimaryAvailabilityPlanRule {
    override fun check(item: PrimaryAvailabilityPlan): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary availability plan. */
data class PrimaryAvailabilityPlanValidator(var rules: MutableList<PrimaryAvailabilityPlanRule>) {
    fun validate(item: PrimaryAvailabilityPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryAvailabilityPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryAvailabilityPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryAvailabilityPlanValidator(): PrimaryAvailabilityPlanValidator {
    return PrimaryAvailabilityPlanValidator(
        mutableListOf<PrimaryAvailabilityPlanRule>(
            PrimaryAvailabilityPlanDiscountRangeRule(7.3, 1871.2),
            PrimaryAvailabilityPlanAllowedDockRule(mutableListOf<String>()),
            PrimaryAvailabilityPlanOwnerIdLengthRule(120),
            PrimaryAvailabilityPlanDockLengthRule(80),
        ),
    )
}
