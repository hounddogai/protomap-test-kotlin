package com.acme.transport.lastmile.drop.batcheddropnotice

/** A rule a drop batch must pass. Check returns its problems, or none. */
interface DropBatchRule {
    fun check(item: DropBatch): MutableList<String>
}

data class DropBatchRequiredChecksumRule(var message: String) : DropBatchRule {
    override fun check(item: DropBatch): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropBatchOdometerRangeRule(var min: Int, var max: Int) : DropBatchRule {
    override fun check(item: DropBatch): MutableList<String> {
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

data class DropBatchCostRangeRule(var min: Double, var max: Double) : DropBatchRule {
    override fun check(item: DropBatch): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop batch. */
data class DropBatchValidator(var rules: MutableList<DropBatchRule>) {
    fun validate(item: DropBatch): MutableList<String> {
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

    fun requireValid(item: DropBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultDropBatchValidator(): DropBatchValidator {
    return DropBatchValidator(
        mutableListOf<DropBatchRule>(
            DropBatchRequiredChecksumRule("drop batch needs this field"),
            DropBatchOdometerRangeRule(80, 86400),
            DropBatchCostRangeRule(4.3, 609.7),
        ),
    )
}
