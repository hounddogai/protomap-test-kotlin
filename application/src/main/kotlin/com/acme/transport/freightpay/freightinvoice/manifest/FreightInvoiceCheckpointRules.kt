package com.acme.transport.freightpay.freightinvoice.manifest

/** A rule a freight invoice checkpoint must pass. Check returns its problems, or none. */
interface FreightInvoiceCheckpointRule {
    fun check(item: FreightInvoiceCheckpoint): MutableList<String>
}

data class FreightInvoiceCheckpointPinnedGuardRule(var limit: Int) : FreightInvoiceCheckpointRule {
    override fun check(item: FreightInvoiceCheckpoint): MutableList<String> {
        if (item.pinned && item.stopCount > limit) {
            return mutableListOf<String>("stop count ${item.stopCount} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceCheckpointCodeLengthRule(var maxLength: Int) : FreightInvoiceCheckpointRule {
    override fun check(item: FreightInvoiceCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceCheckpointNameLengthRule(var maxLength: Int) : FreightInvoiceCheckpointRule {
    override fun check(item: FreightInvoiceCheckpoint): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceCheckpointRequiredCodeRule(var message: String) : FreightInvoiceCheckpointRule {
    override fun check(item: FreightInvoiceCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceCheckpointSourceLengthRule(var maxLength: Int) : FreightInvoiceCheckpointRule {
    override fun check(item: FreightInvoiceCheckpoint): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceCheckpointCodePrefixRule(var prefix: String) : FreightInvoiceCheckpointRule {
    override fun check(item: FreightInvoiceCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice checkpoint. */
data class FreightInvoiceCheckpointValidator(var rules: MutableList<FreightInvoiceCheckpointRule>) {
    fun validate(item: FreightInvoiceCheckpoint): MutableList<String> {
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

    fun requireValid(item: FreightInvoiceCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceCheckpointValidator(): FreightInvoiceCheckpointValidator {
    return FreightInvoiceCheckpointValidator(
        mutableListOf<FreightInvoiceCheckpointRule>(
            FreightInvoiceCheckpointPinnedGuardRule(120),
            FreightInvoiceCheckpointCodeLengthRule(86400),
            FreightInvoiceCheckpointNameLengthRule(80),
            FreightInvoiceCheckpointRequiredCodeRule("freight invoice checkpoint needs an entry"),
            FreightInvoiceCheckpointSourceLengthRule(86400),
            FreightInvoiceCheckpointCodePrefixRule("ACM"),
        ),
    )
}
