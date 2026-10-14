package com.acme.transport.linehaul.trailer.trailerbatch

/** A rule a batched trailer profile must pass. Check returns its problems, or none. */
interface BatchedTrailerProfileRule {
    fun check(item: BatchedTrailerProfile): MutableList<String>
}

data class BatchedTrailerProfileCodePrefixRule(var prefix: String) : BatchedTrailerProfileRule {
    override fun check(item: BatchedTrailerProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerProfileRequiredDriverIdRule(var message: String) : BatchedTrailerProfileRule {
    override fun check(item: BatchedTrailerProfile): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerProfileCodeLengthRule(var maxLength: Int) : BatchedTrailerProfileRule {
    override fun check(item: BatchedTrailerProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched trailer profile. */
data class BatchedTrailerProfileValidator(var rules: MutableList<BatchedTrailerProfileRule>) {
    fun validate(item: BatchedTrailerProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedTrailerProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTrailerProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTrailerProfileValidator(): BatchedTrailerProfileValidator {
    return BatchedTrailerProfileValidator(
        mutableListOf<BatchedTrailerProfileRule>(
            BatchedTrailerProfileCodePrefixRule("X"),
            BatchedTrailerProfileRequiredDriverIdRule("batched trailer profile needs an entry"),
            BatchedTrailerProfileCodeLengthRule(1000),
        ),
    )
}
