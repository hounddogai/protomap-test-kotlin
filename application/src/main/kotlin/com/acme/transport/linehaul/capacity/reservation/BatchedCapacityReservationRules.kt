package com.acme.transport.linehaul.capacity.reservation

import com.acme.transport.kit.text.normalize

/** A rule a batched capacity reservation must pass. Check returns its problems, or none. */
interface BatchedCapacityReservationRule {
    fun check(item: BatchedCapacityReservation): MutableList<String>
}

data class BatchedCapacityReservationRequiredCarrierRule(var message: String) : BatchedCapacityReservationRule {
    override fun check(item: BatchedCapacityReservation): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacityReservationAllowedCarrierRule(
    var allowed: MutableList<String>,
) : BatchedCapacityReservationRule {
    override fun check(item: BatchedCapacityReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacityReservationThresholdRangeRule(
    var min: Double,
    var max: Double,
) : BatchedCapacityReservationRule {
    override fun check(item: BatchedCapacityReservation): MutableList<String> {
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

data class BatchedCapacityReservationVersionRangeRule(
    var min: Int,
    var max: Int,
) : BatchedCapacityReservationRule {
    override fun check(item: BatchedCapacityReservation): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class BatchedCapacityReservationRequiredSegmentRule(var message: String) : BatchedCapacityReservationRule {
    override fun check(item: BatchedCapacityReservation): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched capacity reservation. */
data class BatchedCapacityReservationValidator(var rules: MutableList<BatchedCapacityReservationRule>) {
    fun validate(item: BatchedCapacityReservation): MutableList<String> {
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

    fun requireValid(item: BatchedCapacityReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedCapacityReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedCapacityReservationValidator(): BatchedCapacityReservationValidator {
    return BatchedCapacityReservationValidator(
        mutableListOf<BatchedCapacityReservationRule>(
            BatchedCapacityReservationRequiredCarrierRule("batched capacity reservation needs an entry"),
            BatchedCapacityReservationAllowedCarrierRule(mutableListOf<String>("scheduled", "lenient")),
            BatchedCapacityReservationThresholdRangeRule(1.8, 500.8),
            BatchedCapacityReservationVersionRangeRule(64, 120),
            BatchedCapacityReservationRequiredSegmentRule("batched capacity reservation needs an entry"),
        ),
    )
}
