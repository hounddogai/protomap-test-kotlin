package com.acme.transport.lastmile.proofofdelivery.assignment

/** A rule a proof of delivery assignment must pass. Check returns its problems, or none. */
interface ProofOfDeliveryAssignmentRule {
    fun check(item: ProofOfDeliveryAssignment): MutableList<String>
}

data class ProofOfDeliveryAssignmentNameLengthRule(var maxLength: Int) : ProofOfDeliveryAssignmentRule {
    override fun check(item: ProofOfDeliveryAssignment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryAssignmentReviewerLengthRule(var maxLength: Int) : ProofOfDeliveryAssignmentRule {
    override fun check(item: ProofOfDeliveryAssignment): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryAssignmentCodeLengthRule(var maxLength: Int) : ProofOfDeliveryAssignmentRule {
    override fun check(item: ProofOfDeliveryAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryAssignmentPriorityRangeRule(
    var min: Int,
    var max: Int,
) : ProofOfDeliveryAssignmentRule {
    override fun check(item: ProofOfDeliveryAssignment): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryAssignmentRequiredCodeRule(var message: String) : ProofOfDeliveryAssignmentRule {
    override fun check(item: ProofOfDeliveryAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery assignment. */
data class ProofOfDeliveryAssignmentValidator(var rules: MutableList<ProofOfDeliveryAssignmentRule>) {
    fun validate(item: ProofOfDeliveryAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ProofOfDeliveryAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryAssignmentValidator(): ProofOfDeliveryAssignmentValidator {
    return ProofOfDeliveryAssignmentValidator(
        mutableListOf<ProofOfDeliveryAssignmentRule>(
            ProofOfDeliveryAssignmentNameLengthRule(64),
            ProofOfDeliveryAssignmentReviewerLengthRule(1000),
            ProofOfDeliveryAssignmentCodeLengthRule(16),
            ProofOfDeliveryAssignmentPriorityRangeRule(80, 3600),
            ProofOfDeliveryAssignmentRequiredCodeRule("proof of delivery assignment needs a value"),
        ),
    )
}
