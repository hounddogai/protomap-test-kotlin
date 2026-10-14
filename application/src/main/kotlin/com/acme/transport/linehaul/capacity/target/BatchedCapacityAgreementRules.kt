package com.acme.transport.linehaul.capacity.target

/** A rule a batched capacity agreement must pass. Check returns its problems, or none. */
interface BatchedCapacityAgreementRule {
    fun check(item: BatchedCapacityAgreement): MutableList<String>
}

data class BatchedCapacityAgreementRequiredDriverIdRule(var message: String) : BatchedCapacityAgreementRule {
    override fun check(item: BatchedCapacityAgreement): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacityAgreementCodePrefixRule(var prefix: String) : BatchedCapacityAgreementRule {
    override fun check(item: BatchedCapacityAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacityAgreementRequiredTitleRule(var message: String) : BatchedCapacityAgreementRule {
    override fun check(item: BatchedCapacityAgreement): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched capacity agreement. */
data class BatchedCapacityAgreementValidator(var rules: MutableList<BatchedCapacityAgreementRule>) {
    fun validate(item: BatchedCapacityAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedCapacityAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedCapacityAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedCapacityAgreementValidator(): BatchedCapacityAgreementValidator {
    return BatchedCapacityAgreementValidator(
        mutableListOf<BatchedCapacityAgreementRule>(
            BatchedCapacityAgreementRequiredDriverIdRule("batched capacity agreement needs a value"),
            BatchedCapacityAgreementCodePrefixRule("REF"),
            BatchedCapacityAgreementRequiredTitleRule("batched capacity agreement needs an entry"),
        ),
    )
}
