package com.acme.transport.freightpay.accessorial.accessorialmetric

/** A rule a batched accessorial reservation must pass. Check returns its problems, or none. */
interface BatchedAccessorialReservationRule {
    fun check(item: BatchedAccessorialReservation): MutableList<String>
}

data class BatchedAccessorialReservationPriorityRangeRule(
    var min: Int,
    var max: Int,
) : BatchedAccessorialReservationRule {
    override fun check(item: BatchedAccessorialReservation): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class BatchedAccessorialReservationRequiredTitleRule(
    var message: String,
) : BatchedAccessorialReservationRule {
    override fun check(item: BatchedAccessorialReservation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAccessorialReservationTitleLengthRule(
    var maxLength: Int,
) : BatchedAccessorialReservationRule {
    override fun check(item: BatchedAccessorialReservation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAccessorialReservationRequiredCodeRule(
    var message: String,
) : BatchedAccessorialReservationRule {
    override fun check(item: BatchedAccessorialReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched accessorial reservation. */
data class BatchedAccessorialReservationValidator(var rules: MutableList<BatchedAccessorialReservationRule>) {
    fun validate(item: BatchedAccessorialReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedAccessorialReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAccessorialReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAccessorialReservationValidator(): BatchedAccessorialReservationValidator {
    return BatchedAccessorialReservationValidator(
        mutableListOf<BatchedAccessorialReservationRule>(
            BatchedAccessorialReservationPriorityRangeRule(120, 255),
            BatchedAccessorialReservationRequiredTitleRule("batched accessorial reservation needs an entry"),
            BatchedAccessorialReservationTitleLengthRule(16),
            BatchedAccessorialReservationRequiredCodeRule("batched accessorial reservation needs a value"),
        ),
    )
}
