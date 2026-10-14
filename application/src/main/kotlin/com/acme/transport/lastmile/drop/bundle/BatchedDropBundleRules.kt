package com.acme.transport.lastmile.drop.bundle

/** A rule a batched drop bundle must pass. Check returns its problems, or none. */
interface BatchedDropBundleRule {
    fun check(item: BatchedDropBundle): MutableList<String>
}

data class BatchedDropBundleRequiredCodeRule(var message: String) : BatchedDropBundleRule {
    override fun check(item: BatchedDropBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDropBundleRequiredDepotRule(var message: String) : BatchedDropBundleRule {
    override fun check(item: BatchedDropBundle): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDropBundleCountryLengthRule(var maxLength: Int) : BatchedDropBundleRule {
    override fun check(item: BatchedDropBundle): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDropBundleEtaMinutesRangeRule(var min: Int, var max: Int) : BatchedDropBundleRule {
    override fun check(item: BatchedDropBundle): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched drop bundle. */
data class BatchedDropBundleValidator(var rules: MutableList<BatchedDropBundleRule>) {
    fun validate(item: BatchedDropBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedDropBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDropBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDropBundleValidator(): BatchedDropBundleValidator {
    return BatchedDropBundleValidator(
        mutableListOf<BatchedDropBundleRule>(
            BatchedDropBundleRequiredCodeRule("batched drop bundle needs this field"),
            BatchedDropBundleRequiredDepotRule("batched drop bundle needs a value"),
            BatchedDropBundleCountryLengthRule(3600),
            BatchedDropBundleEtaMinutesRangeRule(64, 80),
        ),
    )
}
