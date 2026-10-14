package com.acme.transport.network.cutoff.ledger

/** A rule a batched cutoff reservation must pass. Check returns its problems, or none. */
interface BatchedCutoffReservationRule {
    fun check(item: BatchedCutoffReservation): MutableList<String>
}

data class BatchedCutoffReservationNameLengthRule(var maxLength: Int) : BatchedCutoffReservationRule {
    override fun check(item: BatchedCutoffReservation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCutoffReservationRequiredCodeRule(var message: String) : BatchedCutoffReservationRule {
    override fun check(item: BatchedCutoffReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedCutoffReservationCodePrefixRule(var prefix: String) : BatchedCutoffReservationRule {
    override fun check(item: BatchedCutoffReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCutoffReservationFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : BatchedCutoffReservationRule {
    override fun check(item: BatchedCutoffReservation): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class BatchedCutoffReservationRequiredExternalRefRule(var message: String) : BatchedCutoffReservationRule {
    override fun check(item: BatchedCutoffReservation): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched cutoff reservation. */
data class BatchedCutoffReservationValidator(var rules: MutableList<BatchedCutoffReservationRule>) {
    fun validate(item: BatchedCutoffReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedCutoffReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedCutoffReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedCutoffReservationValidator(): BatchedCutoffReservationValidator {
    return BatchedCutoffReservationValidator(
        mutableListOf<BatchedCutoffReservationRule>(
            BatchedCutoffReservationNameLengthRule(16),
            BatchedCutoffReservationRequiredCodeRule("batched cutoff reservation needs this field"),
            BatchedCutoffReservationCodePrefixRule("OPS"),
            BatchedCutoffReservationFuelLitersRangeRule(4.1, 4081.2),
            BatchedCutoffReservationRequiredExternalRefRule("batched cutoff reservation needs this field"),
        ),
    )
}
