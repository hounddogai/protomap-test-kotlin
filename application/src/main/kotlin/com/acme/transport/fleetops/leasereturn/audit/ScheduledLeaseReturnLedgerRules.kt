package com.acme.transport.fleetops.leasereturn.audit

/** A rule a scheduled lease return ledger must pass. Check returns its problems, or none. */
interface ScheduledLeaseReturnLedgerRule {
    fun check(item: ScheduledLeaseReturnLedger): MutableList<String>
}

data class ScheduledLeaseReturnLedgerRequiredPhoneRule(var message: String) : ScheduledLeaseReturnLedgerRule {
    override fun check(item: ScheduledLeaseReturnLedger): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLeaseReturnLedgerPhoneLengthRule(var maxLength: Int) : ScheduledLeaseReturnLedgerRule {
    override fun check(item: ScheduledLeaseReturnLedger): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLeaseReturnLedgerTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : ScheduledLeaseReturnLedgerRule {
    override fun check(item: ScheduledLeaseReturnLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class ScheduledLeaseReturnLedgerLabelLengthRule(var maxLength: Int) : ScheduledLeaseReturnLedgerRule {
    override fun check(item: ScheduledLeaseReturnLedger): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled lease return ledger. */
data class ScheduledLeaseReturnLedgerValidator(var rules: MutableList<ScheduledLeaseReturnLedgerRule>) {
    fun validate(item: ScheduledLeaseReturnLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledLeaseReturnLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledLeaseReturnLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledLeaseReturnLedgerValidator(): ScheduledLeaseReturnLedgerValidator {
    return ScheduledLeaseReturnLedgerValidator(
        mutableListOf<ScheduledLeaseReturnLedgerRule>(
            ScheduledLeaseReturnLedgerRequiredPhoneRule("scheduled lease return ledger needs an entry"),
            ScheduledLeaseReturnLedgerPhoneLengthRule(16),
            ScheduledLeaseReturnLedgerTagsLimitRule(86400, mutableListOf<String>("manual", "rolling")),
            ScheduledLeaseReturnLedgerLabelLengthRule(120),
        ),
    )
}
