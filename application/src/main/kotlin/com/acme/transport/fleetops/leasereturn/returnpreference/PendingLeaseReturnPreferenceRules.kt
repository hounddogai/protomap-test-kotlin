package com.acme.transport.fleetops.leasereturn.returnpreference

/** A rule a pending lease return preference must pass. Check returns its problems, or none. */
interface PendingLeaseReturnPreferenceRule {
    fun check(item: PendingLeaseReturnPreference): MutableList<String>
}

data class PendingLeaseReturnPreferenceReviewerLengthRule(
    var maxLength: Int,
) : PendingLeaseReturnPreferenceRule {
    override fun check(item: PendingLeaseReturnPreference): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingLeaseReturnPreferenceRequiredDepotRule(
    var message: String,
) : PendingLeaseReturnPreferenceRule {
    override fun check(item: PendingLeaseReturnPreference): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingLeaseReturnPreferenceCodeLengthRule(var maxLength: Int) : PendingLeaseReturnPreferenceRule {
    override fun check(item: PendingLeaseReturnPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingLeaseReturnPreferenceRequiredCodeRule(
    var message: String,
) : PendingLeaseReturnPreferenceRule {
    override fun check(item: PendingLeaseReturnPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending lease return preference. */
data class PendingLeaseReturnPreferenceValidator(var rules: MutableList<PendingLeaseReturnPreferenceRule>) {
    fun validate(item: PendingLeaseReturnPreference): MutableList<String> {
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

    fun requireValid(item: PendingLeaseReturnPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingLeaseReturnPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingLeaseReturnPreferenceValidator(): PendingLeaseReturnPreferenceValidator {
    return PendingLeaseReturnPreferenceValidator(
        mutableListOf<PendingLeaseReturnPreferenceRule>(
            PendingLeaseReturnPreferenceReviewerLengthRule(32),
            PendingLeaseReturnPreferenceRequiredDepotRule("pending lease return preference needs an entry"),
            PendingLeaseReturnPreferenceCodeLengthRule(32),
            PendingLeaseReturnPreferenceRequiredCodeRule("pending lease return preference needs an entry"),
        ),
    )
}
