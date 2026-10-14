package com.acme.transport.emissions.emissionfactor.cycle

/** A rule a batched emission factor request must pass. Check returns its problems, or none. */
interface BatchedEmissionFactorRequestRule {
    fun check(item: BatchedEmissionFactorRequest): MutableList<String>
}

data class BatchedEmissionFactorRequestCodeLengthRule(var maxLength: Int) : BatchedEmissionFactorRequestRule {
    override fun check(item: BatchedEmissionFactorRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedEmissionFactorRequestRequiredCodeRule(
    var message: String,
) : BatchedEmissionFactorRequestRule {
    override fun check(item: BatchedEmissionFactorRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedEmissionFactorRequestOdometerRangeRule(
    var min: Int,
    var max: Int,
) : BatchedEmissionFactorRequestRule {
    override fun check(item: BatchedEmissionFactorRequest): MutableList<String> {
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

data class BatchedEmissionFactorRequestTitleLengthRule(var maxLength: Int) : BatchedEmissionFactorRequestRule {
    override fun check(item: BatchedEmissionFactorRequest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedEmissionFactorRequestRequiredChecksumRule(
    var message: String,
) : BatchedEmissionFactorRequestRule {
    override fun check(item: BatchedEmissionFactorRequest): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched emission factor request. */
data class BatchedEmissionFactorRequestValidator(var rules: MutableList<BatchedEmissionFactorRequestRule>) {
    fun validate(item: BatchedEmissionFactorRequest): MutableList<String> {
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

    fun requireValid(item: BatchedEmissionFactorRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedEmissionFactorRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedEmissionFactorRequestValidator(): BatchedEmissionFactorRequestValidator {
    return BatchedEmissionFactorRequestValidator(
        mutableListOf<BatchedEmissionFactorRequestRule>(
            BatchedEmissionFactorRequestCodeLengthRule(32),
            BatchedEmissionFactorRequestRequiredCodeRule("batched emission factor request needs a value"),
            BatchedEmissionFactorRequestOdometerRangeRule(16, 3600),
            BatchedEmissionFactorRequestTitleLengthRule(255),
            BatchedEmissionFactorRequestRequiredChecksumRule("batched emission factor request needs a value"),
        ),
    )
}
