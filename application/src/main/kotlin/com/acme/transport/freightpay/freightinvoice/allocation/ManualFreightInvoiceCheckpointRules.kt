package com.acme.transport.freightpay.freightinvoice.allocation

/** A rule a manual freight invoice checkpoint must pass. Check returns its problems, or none. */
interface ManualFreightInvoiceCheckpointRule {
    fun check(item: ManualFreightInvoiceCheckpoint): MutableList<String>
}

data class ManualFreightInvoiceCheckpointVisibleGuardRule(
    var limit: Double,
) : ManualFreightInvoiceCheckpointRule {
    override fun check(item: ManualFreightInvoiceCheckpoint): MutableList<String> {
        if (item.visible && item.weight > limit) {
            return mutableListOf<String>("weight ${item.weight} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class ManualFreightInvoiceCheckpointWeightRangeRule(
    var min: Double,
    var max: Double,
) : ManualFreightInvoiceCheckpointRule {
    override fun check(item: ManualFreightInvoiceCheckpoint): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualFreightInvoiceCheckpointNameLengthRule(
    var maxLength: Int,
) : ManualFreightInvoiceCheckpointRule {
    override fun check(item: ManualFreightInvoiceCheckpoint): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualFreightInvoiceCheckpointCarrierLengthRule(
    var maxLength: Int,
) : ManualFreightInvoiceCheckpointRule {
    override fun check(item: ManualFreightInvoiceCheckpoint): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualFreightInvoiceCheckpointCodeLengthRule(
    var maxLength: Int,
) : ManualFreightInvoiceCheckpointRule {
    override fun check(item: ManualFreightInvoiceCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual freight invoice checkpoint. */
data class ManualFreightInvoiceCheckpointValidator(var rules: MutableList<ManualFreightInvoiceCheckpointRule>) {
    fun validate(item: ManualFreightInvoiceCheckpoint): MutableList<String> {
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

    fun requireValid(item: ManualFreightInvoiceCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualFreightInvoiceCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultManualFreightInvoiceCheckpointValidator(): ManualFreightInvoiceCheckpointValidator {
    return ManualFreightInvoiceCheckpointValidator(
        mutableListOf<ManualFreightInvoiceCheckpointRule>(
            ManualFreightInvoiceCheckpointVisibleGuardRule(7.0),
            ManualFreightInvoiceCheckpointWeightRangeRule(7.0, 4698.6),
            ManualFreightInvoiceCheckpointNameLengthRule(3600),
            ManualFreightInvoiceCheckpointCarrierLengthRule(1000),
            ManualFreightInvoiceCheckpointCodeLengthRule(120),
        ),
    )
}
