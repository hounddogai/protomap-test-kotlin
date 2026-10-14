package com.acme.transport.freightpay.fuelsurcharge.target

/** A rule a batched fuel surcharge request must pass. Check returns its problems, or none. */
interface BatchedFuelSurchargeRequestRule {
    fun check(item: BatchedFuelSurchargeRequest): MutableList<String>
}

data class BatchedFuelSurchargeRequestRequiredTitleRule(var message: String) : BatchedFuelSurchargeRequestRule {
    override fun check(item: BatchedFuelSurchargeRequest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedFuelSurchargeRequestChecksumLengthRule(var maxLength: Int) : BatchedFuelSurchargeRequestRule {
    override fun check(item: BatchedFuelSurchargeRequest): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedFuelSurchargeRequestCodePrefixRule(var prefix: String) : BatchedFuelSurchargeRequestRule {
    override fun check(item: BatchedFuelSurchargeRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedFuelSurchargeRequestDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : BatchedFuelSurchargeRequestRule {
    override fun check(item: BatchedFuelSurchargeRequest): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched fuel surcharge request. */
data class BatchedFuelSurchargeRequestValidator(var rules: MutableList<BatchedFuelSurchargeRequestRule>) {
    fun validate(item: BatchedFuelSurchargeRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedFuelSurchargeRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedFuelSurchargeRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedFuelSurchargeRequestValidator(): BatchedFuelSurchargeRequestValidator {
    return BatchedFuelSurchargeRequestValidator(
        mutableListOf<BatchedFuelSurchargeRequestRule>(
            BatchedFuelSurchargeRequestRequiredTitleRule("batched fuel surcharge request needs an entry"),
            BatchedFuelSurchargeRequestChecksumLengthRule(3600),
            BatchedFuelSurchargeRequestCodePrefixRule("OPS"),
            BatchedFuelSurchargeRequestDurationMinutesRangeRule(120, 1000),
        ),
    )
}
