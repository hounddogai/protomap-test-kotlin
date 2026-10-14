package com.acme.transport.linehaul.capacity.target

import com.acme.transport.kit.text.normalize

/** A rule a pending capacity entry must pass. Check returns its problems, or none. */
interface PendingCapacityEntryRule {
    fun check(item: PendingCapacityEntry): MutableList<String>
}

data class PendingCapacityEntryLoadKgRangeRule(var min: Double, var max: Double) : PendingCapacityEntryRule {
    override fun check(item: PendingCapacityEntry): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingCapacityEntryLabelLengthRule(var maxLength: Int) : PendingCapacityEntryRule {
    override fun check(item: PendingCapacityEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingCapacityEntryAllowedCodeRule(var allowed: MutableList<String>) : PendingCapacityEntryRule {
    override fun check(item: PendingCapacityEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class PendingCapacityEntryRequiredChecksumRule(var message: String) : PendingCapacityEntryRule {
    override fun check(item: PendingCapacityEntry): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingCapacityEntryCodeLengthRule(var maxLength: Int) : PendingCapacityEntryRule {
    override fun check(item: PendingCapacityEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingCapacityEntryCodePrefixRule(var prefix: String) : PendingCapacityEntryRule {
    override fun check(item: PendingCapacityEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending capacity entry. */
data class PendingCapacityEntryValidator(var rules: MutableList<PendingCapacityEntryRule>) {
    fun validate(item: PendingCapacityEntry): MutableList<String> {
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

    fun requireValid(item: PendingCapacityEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCapacityEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCapacityEntryValidator(): PendingCapacityEntryValidator {
    return PendingCapacityEntryValidator(
        mutableListOf<PendingCapacityEntryRule>(
            PendingCapacityEntryLoadKgRangeRule(7.5, 2677.6),
            PendingCapacityEntryLabelLengthRule(64),
            PendingCapacityEntryAllowedCodeRule(mutableListOf<String>("lenient", "lenient", "legacy")),
            PendingCapacityEntryRequiredChecksumRule("pending capacity entry needs an entry"),
            PendingCapacityEntryCodeLengthRule(1000),
            PendingCapacityEntryCodePrefixRule("Q"),
        ),
    )
}
