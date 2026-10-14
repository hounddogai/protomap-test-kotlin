package com.acme.transport.linehaul.tender.entry

/** A rule a primary tender entry must pass. Check returns its problems, or none. */
interface PrimaryTenderEntryRule {
    fun check(item: PrimaryTenderEntry): MutableList<String>
}

data class PrimaryTenderEntryEndsAtRangeRule(var min: Int, var max: Int) : PrimaryTenderEntryRule {
    override fun check(item: PrimaryTenderEntry): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class PrimaryTenderEntryRequiredChecksumRule(var message: String) : PrimaryTenderEntryRule {
    override fun check(item: PrimaryTenderEntry): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTenderEntryCodeLengthRule(var maxLength: Int) : PrimaryTenderEntryRule {
    override fun check(item: PrimaryTenderEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary tender entry. */
data class PrimaryTenderEntryValidator(var rules: MutableList<PrimaryTenderEntryRule>) {
    fun validate(item: PrimaryTenderEntry): MutableList<String> {
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

    fun requireValid(item: PrimaryTenderEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTenderEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTenderEntryValidator(): PrimaryTenderEntryValidator {
    return PrimaryTenderEntryValidator(
        mutableListOf<PrimaryTenderEntryRule>(
            PrimaryTenderEntryEndsAtRangeRule(80, 1000),
            PrimaryTenderEntryRequiredChecksumRule("primary tender entry needs an entry"),
            PrimaryTenderEntryCodeLengthRule(16),
        ),
    )
}
