package com.acme.transport.crewing.availability.cycle

/** A rule a batched availability group must pass. Check returns its problems, or none. */
interface BatchedAvailabilityGroupRule {
    fun check(item: BatchedAvailabilityGroup): MutableList<String>
}

data class BatchedAvailabilityGroupLabelLengthRule(var maxLength: Int) : BatchedAvailabilityGroupRule {
    override fun check(item: BatchedAvailabilityGroup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAvailabilityGroupOdometerRangeRule(
    var min: Int,
    var max: Int,
) : BatchedAvailabilityGroupRule {
    override fun check(item: BatchedAvailabilityGroup): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class BatchedAvailabilityGroupCodeLengthRule(var maxLength: Int) : BatchedAvailabilityGroupRule {
    override fun check(item: BatchedAvailabilityGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAvailabilityGroupRequiredChecksumRule(var message: String) : BatchedAvailabilityGroupRule {
    override fun check(item: BatchedAvailabilityGroup): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched availability group. */
data class BatchedAvailabilityGroupValidator(var rules: MutableList<BatchedAvailabilityGroupRule>) {
    fun validate(item: BatchedAvailabilityGroup): MutableList<String> {
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

    fun requireValid(item: BatchedAvailabilityGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAvailabilityGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAvailabilityGroupValidator(): BatchedAvailabilityGroupValidator {
    return BatchedAvailabilityGroupValidator(
        mutableListOf<BatchedAvailabilityGroupRule>(
            BatchedAvailabilityGroupLabelLengthRule(80),
            BatchedAvailabilityGroupOdometerRangeRule(1000, 1000),
            BatchedAvailabilityGroupCodeLengthRule(86400),
            BatchedAvailabilityGroupRequiredChecksumRule("batched availability group needs an entry"),
        ),
    )
}
