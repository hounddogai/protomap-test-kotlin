package com.acme.transport.freightpay.dispute.disputetemplate

import com.acme.transport.kit.text.normalize

/** A rule a manual dispute snapshot must pass. Check returns its problems, or none. */
interface ManualDisputeSnapshotRule {
    fun check(item: ManualDisputeSnapshot): MutableList<String>
}

data class ManualDisputeSnapshotUnitLengthRule(var maxLength: Int) : ManualDisputeSnapshotRule {
    override fun check(item: ManualDisputeSnapshot): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDisputeSnapshotCodePrefixRule(var prefix: String) : ManualDisputeSnapshotRule {
    override fun check(item: ManualDisputeSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualDisputeSnapshotAllowedCodeRule(var allowed: MutableList<String>) : ManualDisputeSnapshotRule {
    override fun check(item: ManualDisputeSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class ManualDisputeSnapshotRequiredPhoneRule(var message: String) : ManualDisputeSnapshotRule {
    override fun check(item: ManualDisputeSnapshot): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDisputeSnapshotCodeLengthRule(var maxLength: Int) : ManualDisputeSnapshotRule {
    override fun check(item: ManualDisputeSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDisputeSnapshotEndsAtRangeRule(var min: Int, var max: Int) : ManualDisputeSnapshotRule {
    override fun check(item: ManualDisputeSnapshot): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual dispute snapshot. */
data class ManualDisputeSnapshotValidator(var rules: MutableList<ManualDisputeSnapshotRule>) {
    fun validate(item: ManualDisputeSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualDisputeSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDisputeSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDisputeSnapshotValidator(): ManualDisputeSnapshotValidator {
    return ManualDisputeSnapshotValidator(
        mutableListOf<ManualDisputeSnapshotRule>(
            ManualDisputeSnapshotUnitLengthRule(64),
            ManualDisputeSnapshotCodePrefixRule("ACM"),
            ManualDisputeSnapshotAllowedCodeRule(mutableListOf<String>("manual")),
            ManualDisputeSnapshotRequiredPhoneRule("manual dispute snapshot needs this field"),
            ManualDisputeSnapshotCodeLengthRule(32),
            ManualDisputeSnapshotEndsAtRangeRule(80, 120),
        ),
    )
}
