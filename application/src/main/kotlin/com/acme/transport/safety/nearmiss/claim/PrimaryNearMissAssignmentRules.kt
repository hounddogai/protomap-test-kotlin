package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.kit.text.normalize

/** A rule a primary near miss assignment must pass. Check returns its problems, or none. */
interface PrimaryNearMissAssignmentRule {
    fun check(item: PrimaryNearMissAssignment): MutableList<String>
}

data class PrimaryNearMissAssignmentLabelLengthRule(var maxLength: Int) : PrimaryNearMissAssignmentRule {
    override fun check(item: PrimaryNearMissAssignment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNearMissAssignmentAllowedCurrencyRule(
    var allowed: MutableList<String>,
) : PrimaryNearMissAssignmentRule {
    override fun check(item: PrimaryNearMissAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.currency))) {
            return mutableListOf<String>("currency ${item.currency} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNearMissAssignmentRequiredPhoneRule(var message: String) : PrimaryNearMissAssignmentRule {
    override fun check(item: PrimaryNearMissAssignment): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNearMissAssignmentCodePrefixRule(var prefix: String) : PrimaryNearMissAssignmentRule {
    override fun check(item: PrimaryNearMissAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary near miss assignment. */
data class PrimaryNearMissAssignmentValidator(var rules: MutableList<PrimaryNearMissAssignmentRule>) {
    fun validate(item: PrimaryNearMissAssignment): MutableList<String> {
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

    fun requireValid(item: PrimaryNearMissAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryNearMissAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryNearMissAssignmentValidator(): PrimaryNearMissAssignmentValidator {
    return PrimaryNearMissAssignmentValidator(
        mutableListOf<PrimaryNearMissAssignmentRule>(
            PrimaryNearMissAssignmentLabelLengthRule(3600),
            PrimaryNearMissAssignmentAllowedCurrencyRule(mutableListOf<String>("weighted")),
            PrimaryNearMissAssignmentRequiredPhoneRule("primary near miss assignment needs a value"),
            PrimaryNearMissAssignmentCodePrefixRule("Q"),
        ),
    )
}
