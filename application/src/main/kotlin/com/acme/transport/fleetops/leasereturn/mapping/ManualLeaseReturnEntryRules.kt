package com.acme.transport.fleetops.leasereturn.mapping

/** A rule a manual lease return entry must pass. Check returns its problems, or none. */
interface ManualLeaseReturnEntryRule {
    fun check(item: ManualLeaseReturnEntry): MutableList<String>
}

data class ManualLeaseReturnEntryAmountRangeRule(
    var min: Double,
    var max: Double,
) : ManualLeaseReturnEntryRule {
    override fun check(item: ManualLeaseReturnEntry): MutableList<String> {
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

data class ManualLeaseReturnEntryCodeLengthRule(var maxLength: Int) : ManualLeaseReturnEntryRule {
    override fun check(item: ManualLeaseReturnEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualLeaseReturnEntryTrailerIdLengthRule(var maxLength: Int) : ManualLeaseReturnEntryRule {
    override fun check(item: ManualLeaseReturnEntry): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualLeaseReturnEntryLabelLengthRule(var maxLength: Int) : ManualLeaseReturnEntryRule {
    override fun check(item: ManualLeaseReturnEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual lease return entry. */
data class ManualLeaseReturnEntryValidator(var rules: MutableList<ManualLeaseReturnEntryRule>) {
    fun validate(item: ManualLeaseReturnEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualLeaseReturnEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualLeaseReturnEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultManualLeaseReturnEntryValidator(): ManualLeaseReturnEntryValidator {
    return ManualLeaseReturnEntryValidator(
        mutableListOf<ManualLeaseReturnEntryRule>(
            ManualLeaseReturnEntryAmountRangeRule(3.3, 284.4),
            ManualLeaseReturnEntryCodeLengthRule(86400),
            ManualLeaseReturnEntryTrailerIdLengthRule(80),
            ManualLeaseReturnEntryLabelLengthRule(120),
        ),
    )
}
