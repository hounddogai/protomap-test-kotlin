package com.acme.transport.freightpay.dispute.disputeassignment

/** A rule a manual dispute assignment must pass. Check returns its problems, or none. */
interface ManualDisputeAssignmentRule {
    fun check(item: ManualDisputeAssignment): MutableList<String>
}

data class ManualDisputeAssignmentCodePrefixRule(var prefix: String) : ManualDisputeAssignmentRule {
    override fun check(item: ManualDisputeAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualDisputeAssignmentEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : ManualDisputeAssignmentRule {
    override fun check(item: ManualDisputeAssignment): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class ManualDisputeAssignmentRequiredCodeRule(var message: String) : ManualDisputeAssignmentRule {
    override fun check(item: ManualDisputeAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual dispute assignment. */
data class ManualDisputeAssignmentValidator(var rules: MutableList<ManualDisputeAssignmentRule>) {
    fun validate(item: ManualDisputeAssignment): MutableList<String> {
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

    fun requireValid(item: ManualDisputeAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDisputeAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDisputeAssignmentValidator(): ManualDisputeAssignmentValidator {
    return ManualDisputeAssignmentValidator(
        mutableListOf<ManualDisputeAssignmentRule>(
            ManualDisputeAssignmentCodePrefixRule("REF"),
            ManualDisputeAssignmentEtaMinutesRangeRule(32, 80),
            ManualDisputeAssignmentRequiredCodeRule("manual dispute assignment needs this field"),
        ),
    )
}
