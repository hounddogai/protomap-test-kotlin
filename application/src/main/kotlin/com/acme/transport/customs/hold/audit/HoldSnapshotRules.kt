package com.acme.transport.customs.hold.audit

/** A rule a hold snapshot must pass. Check returns its problems, or none. */
interface HoldSnapshotRule {
    fun check(item: HoldSnapshot): MutableList<String>
}

data class HoldSnapshotActiveGuardRule(var limit: Int) : HoldSnapshotRule {
    override fun check(item: HoldSnapshot): MutableList<String> {
        if (item.active && item.retries > limit) {
            return mutableListOf<String>("retries ${item.retries} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class HoldSnapshotRequiredPhoneRule(var message: String) : HoldSnapshotRule {
    override fun check(item: HoldSnapshot): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldSnapshotPhoneLengthRule(var maxLength: Int) : HoldSnapshotRule {
    override fun check(item: HoldSnapshot): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold snapshot. */
data class HoldSnapshotValidator(var rules: MutableList<HoldSnapshotRule>) {
    fun validate(item: HoldSnapshot): MutableList<String> {
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

    fun requireValid(item: HoldSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldSnapshotValidator(): HoldSnapshotValidator {
    return HoldSnapshotValidator(
        mutableListOf<HoldSnapshotRule>(
            HoldSnapshotActiveGuardRule(16),
            HoldSnapshotRequiredPhoneRule("hold snapshot needs this field"),
            HoldSnapshotPhoneLengthRule(255),
        ),
    )
}
