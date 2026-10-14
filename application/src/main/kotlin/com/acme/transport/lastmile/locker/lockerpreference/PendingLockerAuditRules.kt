package com.acme.transport.lastmile.locker.lockerpreference

/** A rule a pending locker audit must pass. Check returns its problems, or none. */
interface PendingLockerAuditRule {
    fun check(item: PendingLockerAudit): MutableList<String>
}

data class PendingLockerAuditStopCountRangeRule(var min: Int, var max: Int) : PendingLockerAuditRule {
    override fun check(item: PendingLockerAudit): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingLockerAuditRequiredTitleRule(var message: String) : PendingLockerAuditRule {
    override fun check(item: PendingLockerAudit): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingLockerAuditPhoneLengthRule(var maxLength: Int) : PendingLockerAuditRule {
    override fun check(item: PendingLockerAudit): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingLockerAuditRequiredPhoneRule(var message: String) : PendingLockerAuditRule {
    override fun check(item: PendingLockerAudit): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingLockerAuditRequiredCodeRule(var message: String) : PendingLockerAuditRule {
    override fun check(item: PendingLockerAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingLockerAuditRequiredCategoryRule(var message: String) : PendingLockerAuditRule {
    override fun check(item: PendingLockerAudit): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending locker audit. */
data class PendingLockerAuditValidator(var rules: MutableList<PendingLockerAuditRule>) {
    fun validate(item: PendingLockerAudit): MutableList<String> {
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

    fun requireValid(item: PendingLockerAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingLockerAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingLockerAuditValidator(): PendingLockerAuditValidator {
    return PendingLockerAuditValidator(
        mutableListOf<PendingLockerAuditRule>(
            PendingLockerAuditStopCountRangeRule(16, 16),
            PendingLockerAuditRequiredTitleRule("pending locker audit needs an entry"),
            PendingLockerAuditPhoneLengthRule(16),
            PendingLockerAuditRequiredPhoneRule("pending locker audit needs a value"),
            PendingLockerAuditRequiredCodeRule("pending locker audit needs a value"),
            PendingLockerAuditRequiredCategoryRule("pending locker audit needs an entry"),
        ),
    )
}
