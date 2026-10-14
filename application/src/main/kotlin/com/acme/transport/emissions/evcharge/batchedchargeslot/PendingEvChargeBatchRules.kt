package com.acme.transport.emissions.evcharge.batchedchargeslot

/** A rule a pending ev charge batch must pass. Check returns its problems, or none. */
interface PendingEvChargeBatchRule {
    fun check(item: PendingEvChargeBatch): MutableList<String>
}

data class PendingEvChargeBatchMinimumRangeRule(var min: Double, var max: Double) : PendingEvChargeBatchRule {
    override fun check(item: PendingEvChargeBatch): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingEvChargeBatchThresholdRangeRule(var min: Double, var max: Double) : PendingEvChargeBatchRule {
    override fun check(item: PendingEvChargeBatch): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingEvChargeBatchCodeLengthRule(var maxLength: Int) : PendingEvChargeBatchRule {
    override fun check(item: PendingEvChargeBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingEvChargeBatchRequiredLabelRule(var message: String) : PendingEvChargeBatchRule {
    override fun check(item: PendingEvChargeBatch): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending ev charge batch. */
data class PendingEvChargeBatchValidator(var rules: MutableList<PendingEvChargeBatchRule>) {
    fun validate(item: PendingEvChargeBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingEvChargeBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingEvChargeBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingEvChargeBatchValidator(): PendingEvChargeBatchValidator {
    return PendingEvChargeBatchValidator(
        mutableListOf<PendingEvChargeBatchRule>(
            PendingEvChargeBatchMinimumRangeRule(0.5, 4309.6),
            PendingEvChargeBatchThresholdRangeRule(3.0, 3268.4),
            PendingEvChargeBatchCodeLengthRule(32),
            PendingEvChargeBatchRequiredLabelRule("pending ev charge batch needs a value"),
        ),
    )
}
