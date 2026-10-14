package com.acme.transport.emissions.carbonreport.quota

/** A rule a batched carbon report policy must pass. Check returns its problems, or none. */
interface BatchedCarbonReportPolicyRule {
    fun check(item: BatchedCarbonReportPolicy): MutableList<String>
}

data class BatchedCarbonReportPolicyRequiredLabelRule(var message: String) : BatchedCarbonReportPolicyRule {
    override fun check(item: BatchedCarbonReportPolicy): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedCarbonReportPolicyRequiredOwnerIdRule(var message: String) : BatchedCarbonReportPolicyRule {
    override fun check(item: BatchedCarbonReportPolicy): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedCarbonReportPolicyRetriesRangeRule(
    var min: Int,
    var max: Int,
) : BatchedCarbonReportPolicyRule {
    override fun check(item: BatchedCarbonReportPolicy): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a batched carbon report policy. */
data class BatchedCarbonReportPolicyValidator(var rules: MutableList<BatchedCarbonReportPolicyRule>) {
    fun validate(item: BatchedCarbonReportPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedCarbonReportPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedCarbonReportPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedCarbonReportPolicyValidator(): BatchedCarbonReportPolicyValidator {
    return BatchedCarbonReportPolicyValidator(
        mutableListOf<BatchedCarbonReportPolicyRule>(
            BatchedCarbonReportPolicyRequiredLabelRule("batched carbon report policy needs a value"),
            BatchedCarbonReportPolicyRequiredOwnerIdRule("batched carbon report policy needs an entry"),
            BatchedCarbonReportPolicyRetriesRangeRule(64, 1000),
        ),
    )
}
