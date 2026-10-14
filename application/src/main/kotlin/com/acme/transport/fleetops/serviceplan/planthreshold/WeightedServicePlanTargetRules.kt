package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.kit.text.normalize

/** A rule a weighted service plan target must pass. Check returns its problems, or none. */
interface WeightedServicePlanTargetRule {
    fun check(item: WeightedServicePlanTarget): MutableList<String>
}

data class WeightedServicePlanTargetAllowedCarrierRule(
    var allowed: MutableList<String>,
) : WeightedServicePlanTargetRule {
    override fun check(item: WeightedServicePlanTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class WeightedServicePlanTargetRequiredLabelRule(var message: String) : WeightedServicePlanTargetRule {
    override fun check(item: WeightedServicePlanTarget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedServicePlanTargetCarrierLengthRule(var maxLength: Int) : WeightedServicePlanTargetRule {
    override fun check(item: WeightedServicePlanTarget): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedServicePlanTargetRequiredDescriptionRule(
    var message: String,
) : WeightedServicePlanTargetRule {
    override fun check(item: WeightedServicePlanTarget): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedServicePlanTargetRequiredCodeRule(var message: String) : WeightedServicePlanTargetRule {
    override fun check(item: WeightedServicePlanTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedServicePlanTargetDescriptionLengthRule(var maxLength: Int) : WeightedServicePlanTargetRule {
    override fun check(item: WeightedServicePlanTarget): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted service plan target. */
data class WeightedServicePlanTargetValidator(var rules: MutableList<WeightedServicePlanTargetRule>) {
    fun validate(item: WeightedServicePlanTarget): MutableList<String> {
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

    fun requireValid(item: WeightedServicePlanTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedServicePlanTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedServicePlanTargetValidator(): WeightedServicePlanTargetValidator {
    return WeightedServicePlanTargetValidator(
        mutableListOf<WeightedServicePlanTargetRule>(
            WeightedServicePlanTargetAllowedCarrierRule(mutableListOf<String>("adaptive")),
            WeightedServicePlanTargetRequiredLabelRule("weighted service plan target needs an entry"),
            WeightedServicePlanTargetCarrierLengthRule(120),
            WeightedServicePlanTargetRequiredDescriptionRule("weighted service plan target needs an entry"),
            WeightedServicePlanTargetRequiredCodeRule("weighted service plan target needs a value"),
            WeightedServicePlanTargetDescriptionLengthRule(64),
        ),
    )
}
