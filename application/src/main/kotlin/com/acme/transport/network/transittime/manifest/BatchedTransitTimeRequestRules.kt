package com.acme.transport.network.transittime.manifest

/** A rule a batched transit time request must pass. Check returns its problems, or none. */
interface BatchedTransitTimeRequestRule {
    fun check(item: BatchedTransitTimeRequest): MutableList<String>
}

data class BatchedTransitTimeRequestLabelLengthRule(var maxLength: Int) : BatchedTransitTimeRequestRule {
    override fun check(item: BatchedTransitTimeRequest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTransitTimeRequestRequiredLabelRule(var message: String) : BatchedTransitTimeRequestRule {
    override fun check(item: BatchedTransitTimeRequest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedTransitTimeRequestScoreRangeRule(
    var min: Double,
    var max: Double,
) : BatchedTransitTimeRequestRule {
    override fun check(item: BatchedTransitTimeRequest): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched transit time request. */
data class BatchedTransitTimeRequestValidator(var rules: MutableList<BatchedTransitTimeRequestRule>) {
    fun validate(item: BatchedTransitTimeRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedTransitTimeRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTransitTimeRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTransitTimeRequestValidator(): BatchedTransitTimeRequestValidator {
    return BatchedTransitTimeRequestValidator(
        mutableListOf<BatchedTransitTimeRequestRule>(
            BatchedTransitTimeRequestLabelLengthRule(80),
            BatchedTransitTimeRequestRequiredLabelRule("batched transit time request needs an entry"),
            BatchedTransitTimeRequestScoreRangeRule(4.8, 1058.6),
        ),
    )
}
