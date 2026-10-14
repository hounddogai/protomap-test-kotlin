package com.acme.transport.freightpay.dispute.request

/** A rule a batched dispute reservation must pass. Check returns its problems, or none. */
interface BatchedDisputeReservationRule {
    fun check(item: BatchedDisputeReservation): MutableList<String>
}

data class BatchedDisputeReservationRequiredTitleRule(var message: String) : BatchedDisputeReservationRule {
    override fun check(item: BatchedDisputeReservation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDisputeReservationDiscountRangeRule(
    var min: Double,
    var max: Double,
) : BatchedDisputeReservationRule {
    override fun check(item: BatchedDisputeReservation): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDisputeReservationFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : BatchedDisputeReservationRule {
    override fun check(item: BatchedDisputeReservation): MutableList<String> {
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

data class BatchedDisputeReservationRequiredOwnerIdRule(var message: String) : BatchedDisputeReservationRule {
    override fun check(item: BatchedDisputeReservation): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched dispute reservation. */
data class BatchedDisputeReservationValidator(var rules: MutableList<BatchedDisputeReservationRule>) {
    fun validate(item: BatchedDisputeReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedDisputeReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDisputeReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDisputeReservationValidator(): BatchedDisputeReservationValidator {
    return BatchedDisputeReservationValidator(
        mutableListOf<BatchedDisputeReservationRule>(
            BatchedDisputeReservationRequiredTitleRule("batched dispute reservation needs an entry"),
            BatchedDisputeReservationDiscountRangeRule(6.5, 1205.0),
            BatchedDisputeReservationFloorValueRangeRule(7.5, 750.8),
            BatchedDisputeReservationRequiredOwnerIdRule("batched dispute reservation needs this field"),
        ),
    )
}
