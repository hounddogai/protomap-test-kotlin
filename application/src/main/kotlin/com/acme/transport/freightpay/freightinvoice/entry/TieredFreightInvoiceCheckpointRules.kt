package com.acme.transport.freightpay.freightinvoice.entry

/** A rule a tiered freight invoice checkpoint must pass. Check returns its problems, or none. */
interface TieredFreightInvoiceCheckpointRule {
    fun check(item: TieredFreightInvoiceCheckpoint): MutableList<String>
}

data class TieredFreightInvoiceCheckpointCodeLengthRule(
    var maxLength: Int,
) : TieredFreightInvoiceCheckpointRule {
    override fun check(item: TieredFreightInvoiceCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredFreightInvoiceCheckpointRequiredNameRule(
    var message: String,
) : TieredFreightInvoiceCheckpointRule {
    override fun check(item: TieredFreightInvoiceCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredFreightInvoiceCheckpointCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : TieredFreightInvoiceCheckpointRule {
    override fun check(item: TieredFreightInvoiceCheckpoint): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredFreightInvoiceCheckpointLabelLengthRule(
    var maxLength: Int,
) : TieredFreightInvoiceCheckpointRule {
    override fun check(item: TieredFreightInvoiceCheckpoint): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredFreightInvoiceCheckpointVisibleGuardRule(
    var limit: Double,
) : TieredFreightInvoiceCheckpointRule {
    override fun check(item: TieredFreightInvoiceCheckpoint): MutableList<String> {
        if (item.visible && item.budget > limit) {
            return mutableListOf<String>("budget ${item.budget} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class TieredFreightInvoiceCheckpointNameLengthRule(
    var maxLength: Int,
) : TieredFreightInvoiceCheckpointRule {
    override fun check(item: TieredFreightInvoiceCheckpoint): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered freight invoice checkpoint. */
data class TieredFreightInvoiceCheckpointValidator(var rules: MutableList<TieredFreightInvoiceCheckpointRule>) {
    fun validate(item: TieredFreightInvoiceCheckpoint): MutableList<String> {
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

    fun requireValid(item: TieredFreightInvoiceCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredFreightInvoiceCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredFreightInvoiceCheckpointValidator(): TieredFreightInvoiceCheckpointValidator {
    return TieredFreightInvoiceCheckpointValidator(
        mutableListOf<TieredFreightInvoiceCheckpointRule>(
            TieredFreightInvoiceCheckpointCodeLengthRule(80),
            TieredFreightInvoiceCheckpointRequiredNameRule("tiered freight invoice checkpoint needs an entry"),
            TieredFreightInvoiceCheckpointCreatedAtRangeRule(32, 86400),
            TieredFreightInvoiceCheckpointLabelLengthRule(86400),
            TieredFreightInvoiceCheckpointVisibleGuardRule(6.7),
            TieredFreightInvoiceCheckpointNameLengthRule(32),
        ),
    )
}
