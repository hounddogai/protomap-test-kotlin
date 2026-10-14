package com.acme.transport.freightpay.dispute.slot

/** A rule a dispute record must pass. Check returns its problems, or none. */
interface DisputeRecordRule {
    fun check(item: DisputeRecord): MutableList<String>
}

data class DisputeRecordRequiredLabelRule(var message: String) : DisputeRecordRule {
    override fun check(item: DisputeRecord): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeRecordCodePrefixRule(var prefix: String) : DisputeRecordRule {
    override fun check(item: DisputeRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DisputeRecordLabelLengthRule(var maxLength: Int) : DisputeRecordRule {
    override fun check(item: DisputeRecord): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeRecordWeightRangeRule(var min: Double, var max: Double) : DisputeRecordRule {
    override fun check(item: DisputeRecord): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute record. */
data class DisputeRecordValidator(var rules: MutableList<DisputeRecordRule>) {
    fun validate(item: DisputeRecord): MutableList<String> {
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

    fun requireValid(item: DisputeRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeRecordValidator(): DisputeRecordValidator {
    return DisputeRecordValidator(
        mutableListOf<DisputeRecordRule>(
            DisputeRecordRequiredLabelRule("dispute record needs this field"),
            DisputeRecordCodePrefixRule("OPS"),
            DisputeRecordLabelLengthRule(64),
            DisputeRecordWeightRangeRule(4.5, 3947.6),
        ),
    )
}
