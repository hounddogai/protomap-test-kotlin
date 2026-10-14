package com.acme.transport.crewing.availability.exception

/** A rule a batched availability agreement must pass. Check returns its problems, or none. */
interface BatchedAvailabilityAgreementRule {
    fun check(item: BatchedAvailabilityAgreement): MutableList<String>
}

data class BatchedAvailabilityAgreementDepotLengthRule(var maxLength: Int) : BatchedAvailabilityAgreementRule {
    override fun check(item: BatchedAvailabilityAgreement): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAvailabilityAgreementBudgetRangeRule(
    var min: Double,
    var max: Double,
) : BatchedAvailabilityAgreementRule {
    override fun check(item: BatchedAvailabilityAgreement): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class BatchedAvailabilityAgreementRequiredShiftCodeRule(
    var message: String,
) : BatchedAvailabilityAgreementRule {
    override fun check(item: BatchedAvailabilityAgreement): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAvailabilityAgreementNameLengthRule(var maxLength: Int) : BatchedAvailabilityAgreementRule {
    override fun check(item: BatchedAvailabilityAgreement): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAvailabilityAgreementArchivedGuardRule(var limit: Double) : BatchedAvailabilityAgreementRule {
    override fun check(item: BatchedAvailabilityAgreement): MutableList<String> {
        if (item.archived && item.budget > limit) {
            return mutableListOf<String>("budget ${item.budget} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched availability agreement. */
data class BatchedAvailabilityAgreementValidator(var rules: MutableList<BatchedAvailabilityAgreementRule>) {
    fun validate(item: BatchedAvailabilityAgreement): MutableList<String> {
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

    fun requireValid(item: BatchedAvailabilityAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAvailabilityAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAvailabilityAgreementValidator(): BatchedAvailabilityAgreementValidator {
    return BatchedAvailabilityAgreementValidator(
        mutableListOf<BatchedAvailabilityAgreementRule>(
            BatchedAvailabilityAgreementDepotLengthRule(80),
            BatchedAvailabilityAgreementBudgetRangeRule(4.8, 2630.6),
            BatchedAvailabilityAgreementRequiredShiftCodeRule("batched availability agreement needs an entry"),
            BatchedAvailabilityAgreementNameLengthRule(120),
            BatchedAvailabilityAgreementArchivedGuardRule(3.4),
        ),
    )
}
