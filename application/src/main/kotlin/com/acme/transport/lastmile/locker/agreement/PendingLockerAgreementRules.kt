package com.acme.transport.lastmile.locker.agreement

/** A rule a pending locker agreement must pass. Check returns its problems, or none. */
interface PendingLockerAgreementRule {
    fun check(item: PendingLockerAgreement): MutableList<String>
}

data class PendingLockerAgreementCodeLengthRule(var maxLength: Int) : PendingLockerAgreementRule {
    override fun check(item: PendingLockerAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingLockerAgreementCodePrefixRule(var prefix: String) : PendingLockerAgreementRule {
    override fun check(item: PendingLockerAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingLockerAgreementRequiredOwnerIdRule(var message: String) : PendingLockerAgreementRule {
    override fun check(item: PendingLockerAgreement): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending locker agreement. */
data class PendingLockerAgreementValidator(var rules: MutableList<PendingLockerAgreementRule>) {
    fun validate(item: PendingLockerAgreement): MutableList<String> {
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

    fun requireValid(item: PendingLockerAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingLockerAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingLockerAgreementValidator(): PendingLockerAgreementValidator {
    return PendingLockerAgreementValidator(
        mutableListOf<PendingLockerAgreementRule>(
            PendingLockerAgreementCodeLengthRule(255),
            PendingLockerAgreementCodePrefixRule("OPS"),
            PendingLockerAgreementRequiredOwnerIdRule("pending locker agreement needs this field"),
        ),
    )
}
