package com.acme.transport.fleetops.serviceplan.planprofile

import com.acme.transport.kit.text.normalize

/** A rule a batched service plan reservation must pass. Check returns its problems, or none. */
interface BatchedServicePlanReservationRule {
    fun check(item: BatchedServicePlanReservation): MutableList<String>
}

data class BatchedServicePlanReservationCurrencyLengthRule(
    var maxLength: Int,
) : BatchedServicePlanReservationRule {
    override fun check(item: BatchedServicePlanReservation): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedServicePlanReservationThresholdRangeRule(
    var min: Double,
    var max: Double,
) : BatchedServicePlanReservationRule {
    override fun check(item: BatchedServicePlanReservation): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class BatchedServicePlanReservationRequiredCurrencyRule(
    var message: String,
) : BatchedServicePlanReservationRule {
    override fun check(item: BatchedServicePlanReservation): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedServicePlanReservationQuantityRangeRule(
    var min: Int,
    var max: Int,
) : BatchedServicePlanReservationRule {
    override fun check(item: BatchedServicePlanReservation): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class BatchedServicePlanReservationAllowedCodeRule(
    var allowed: MutableList<String>,
) : BatchedServicePlanReservationRule {
    override fun check(item: BatchedServicePlanReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched service plan reservation. */
data class BatchedServicePlanReservationValidator(var rules: MutableList<BatchedServicePlanReservationRule>) {
    fun validate(item: BatchedServicePlanReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedServicePlanReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedServicePlanReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedServicePlanReservationValidator(): BatchedServicePlanReservationValidator {
    return BatchedServicePlanReservationValidator(
        mutableListOf<BatchedServicePlanReservationRule>(
            BatchedServicePlanReservationCurrencyLengthRule(1000),
            BatchedServicePlanReservationThresholdRangeRule(9.4, 2985.3),
            BatchedServicePlanReservationRequiredCurrencyRule("batched service plan reservation needs an entry"),
            BatchedServicePlanReservationQuantityRangeRule(64, 1000),
            BatchedServicePlanReservationAllowedCodeRule(mutableListOf<String>("legacy")),
        ),
    )
}
