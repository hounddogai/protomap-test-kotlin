package com.acme.transport.lastmile.redelivery.redeliveryrule

/** A rule a scheduled redelivery entry must pass. Check returns its problems, or none. */
interface ScheduledRedeliveryEntryRule {
    fun check(item: ScheduledRedeliveryEntry): MutableList<String>
}

data class ScheduledRedeliveryEntryCodeLengthRule(var maxLength: Int) : ScheduledRedeliveryEntryRule {
    override fun check(item: ScheduledRedeliveryEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryEntryOwnerIdLengthRule(var maxLength: Int) : ScheduledRedeliveryEntryRule {
    override fun check(item: ScheduledRedeliveryEntry): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryEntryRequiredReviewerRule(var message: String) : ScheduledRedeliveryEntryRule {
    override fun check(item: ScheduledRedeliveryEntry): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryEntryReviewerLengthRule(var maxLength: Int) : ScheduledRedeliveryEntryRule {
    override fun check(item: ScheduledRedeliveryEntry): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryEntryRequiredCodeRule(var message: String) : ScheduledRedeliveryEntryRule {
    override fun check(item: ScheduledRedeliveryEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRedeliveryEntryRequiredOwnerIdRule(var message: String) : ScheduledRedeliveryEntryRule {
    override fun check(item: ScheduledRedeliveryEntry): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled redelivery entry. */
data class ScheduledRedeliveryEntryValidator(var rules: MutableList<ScheduledRedeliveryEntryRule>) {
    fun validate(item: ScheduledRedeliveryEntry): MutableList<String> {
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

    fun requireValid(item: ScheduledRedeliveryEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRedeliveryEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRedeliveryEntryValidator(): ScheduledRedeliveryEntryValidator {
    return ScheduledRedeliveryEntryValidator(
        mutableListOf<ScheduledRedeliveryEntryRule>(
            ScheduledRedeliveryEntryCodeLengthRule(3600),
            ScheduledRedeliveryEntryOwnerIdLengthRule(86400),
            ScheduledRedeliveryEntryRequiredReviewerRule("scheduled redelivery entry needs a value"),
            ScheduledRedeliveryEntryReviewerLengthRule(16),
            ScheduledRedeliveryEntryRequiredCodeRule("scheduled redelivery entry needs an entry"),
            ScheduledRedeliveryEntryRequiredOwnerIdRule("scheduled redelivery entry needs an entry"),
        ),
    )
}
