package com.acme.transport.linehaul.tender.recorditem

/** A rule a lenient tender checkpoint must pass. Check returns its problems, or none. */
interface LenientTenderCheckpointRule {
    fun check(item: LenientTenderCheckpoint): MutableList<String>
}

data class LenientTenderCheckpointAmountRangeRule(
    var min: Double,
    var max: Double,
) : LenientTenderCheckpointRule {
    override fun check(item: LenientTenderCheckpoint): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class LenientTenderCheckpointUpdatedAtRangeRule(var min: Int, var max: Int) : LenientTenderCheckpointRule {
    override fun check(item: LenientTenderCheckpoint): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class LenientTenderCheckpointRequiredCodeRule(var message: String) : LenientTenderCheckpointRule {
    override fun check(item: LenientTenderCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientTenderCheckpointMinimumRangeRule(
    var min: Double,
    var max: Double,
) : LenientTenderCheckpointRule {
    override fun check(item: LenientTenderCheckpoint): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientTenderCheckpointCarrierLengthRule(var maxLength: Int) : LenientTenderCheckpointRule {
    override fun check(item: LenientTenderCheckpoint): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient tender checkpoint. */
data class LenientTenderCheckpointValidator(var rules: MutableList<LenientTenderCheckpointRule>) {
    fun validate(item: LenientTenderCheckpoint): MutableList<String> {
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

    fun requireValid(item: LenientTenderCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientTenderCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientTenderCheckpointValidator(): LenientTenderCheckpointValidator {
    return LenientTenderCheckpointValidator(
        mutableListOf<LenientTenderCheckpointRule>(
            LenientTenderCheckpointAmountRangeRule(1.9, 4846.0),
            LenientTenderCheckpointUpdatedAtRangeRule(16, 32),
            LenientTenderCheckpointRequiredCodeRule("lenient tender checkpoint needs this field"),
            LenientTenderCheckpointMinimumRangeRule(2.5, 217.9),
            LenientTenderCheckpointCarrierLengthRule(64),
        ),
    )
}
