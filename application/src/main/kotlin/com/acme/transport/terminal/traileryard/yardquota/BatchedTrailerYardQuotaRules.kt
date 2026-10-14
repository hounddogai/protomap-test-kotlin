package com.acme.transport.terminal.traileryard.yardquota

/** A rule a batched trailer yard quota must pass. Check returns its problems, or none. */
interface BatchedTrailerYardQuotaRule {
    fun check(item: BatchedTrailerYardQuota): MutableList<String>
}

data class BatchedTrailerYardQuotaOdometerRangeRule(var min: Int, var max: Int) : BatchedTrailerYardQuotaRule {
    override fun check(item: BatchedTrailerYardQuota): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class BatchedTrailerYardQuotaAttemptsRangeRule(var min: Int, var max: Int) : BatchedTrailerYardQuotaRule {
    override fun check(item: BatchedTrailerYardQuota): MutableList<String> {
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

data class BatchedTrailerYardQuotaMarginRangeRule(
    var min: Double,
    var max: Double,
) : BatchedTrailerYardQuotaRule {
    override fun check(item: BatchedTrailerYardQuota): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched trailer yard quota. */
data class BatchedTrailerYardQuotaValidator(var rules: MutableList<BatchedTrailerYardQuotaRule>) {
    fun validate(item: BatchedTrailerYardQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedTrailerYardQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTrailerYardQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTrailerYardQuotaValidator(): BatchedTrailerYardQuotaValidator {
    return BatchedTrailerYardQuotaValidator(
        mutableListOf<BatchedTrailerYardQuotaRule>(
            BatchedTrailerYardQuotaOdometerRangeRule(32, 3600),
            BatchedTrailerYardQuotaAttemptsRangeRule(120, 255),
            BatchedTrailerYardQuotaMarginRangeRule(4.8, 1805.1),
        ),
    )
}
