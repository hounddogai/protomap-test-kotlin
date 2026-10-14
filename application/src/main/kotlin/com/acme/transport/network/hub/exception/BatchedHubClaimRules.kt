package com.acme.transport.network.hub.exception

/** A rule a batched hub claim must pass. Check returns its problems, or none. */
interface BatchedHubClaimRule {
    fun check(item: BatchedHubClaim): MutableList<String>
}

data class BatchedHubClaimRequiredDescriptionRule(var message: String) : BatchedHubClaimRule {
    override fun check(item: BatchedHubClaim): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedHubClaimRequiredTitleRule(var message: String) : BatchedHubClaimRule {
    override fun check(item: BatchedHubClaim): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedHubClaimRevisionRangeRule(var min: Int, var max: Int) : BatchedHubClaimRule {
    override fun check(item: BatchedHubClaim): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class BatchedHubClaimPhoneLengthRule(var maxLength: Int) : BatchedHubClaimRule {
    override fun check(item: BatchedHubClaim): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedHubClaimAttemptsRangeRule(var min: Int, var max: Int) : BatchedHubClaimRule {
    override fun check(item: BatchedHubClaim): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class BatchedHubClaimRateRangeRule(var min: Double, var max: Double) : BatchedHubClaimRule {
    override fun check(item: BatchedHubClaim): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a batched hub claim. */
data class BatchedHubClaimValidator(var rules: MutableList<BatchedHubClaimRule>) {
    fun validate(item: BatchedHubClaim): MutableList<String> {
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

    fun requireValid(item: BatchedHubClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedHubClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedHubClaimValidator(): BatchedHubClaimValidator {
    return BatchedHubClaimValidator(
        mutableListOf<BatchedHubClaimRule>(
            BatchedHubClaimRequiredDescriptionRule("batched hub claim needs a value"),
            BatchedHubClaimRequiredTitleRule("batched hub claim needs an entry"),
            BatchedHubClaimRevisionRangeRule(80, 120),
            BatchedHubClaimPhoneLengthRule(80),
            BatchedHubClaimAttemptsRangeRule(80, 80),
            BatchedHubClaimRateRangeRule(3.2, 3494.3),
        ),
    )
}
