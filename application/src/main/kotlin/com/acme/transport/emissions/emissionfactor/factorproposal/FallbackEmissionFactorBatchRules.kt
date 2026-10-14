package com.acme.transport.emissions.emissionfactor.factorproposal

/** A rule a fallback emission factor batch must pass. Check returns its problems, or none. */
interface FallbackEmissionFactorBatchRule {
    fun check(item: FallbackEmissionFactorBatch): MutableList<String>
}

data class FallbackEmissionFactorBatchNameLengthRule(var maxLength: Int) : FallbackEmissionFactorBatchRule {
    override fun check(item: FallbackEmissionFactorBatch): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEmissionFactorBatchRequiredNameRule(var message: String) : FallbackEmissionFactorBatchRule {
    override fun check(item: FallbackEmissionFactorBatch): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackEmissionFactorBatchCodeLengthRule(var maxLength: Int) : FallbackEmissionFactorBatchRule {
    override fun check(item: FallbackEmissionFactorBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEmissionFactorBatchCeilingRangeRule(
    var min: Double,
    var max: Double,
) : FallbackEmissionFactorBatchRule {
    override fun check(item: FallbackEmissionFactorBatch): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEmissionFactorBatchRequiredCodeRule(var message: String) : FallbackEmissionFactorBatchRule {
    override fun check(item: FallbackEmissionFactorBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback emission factor batch. */
data class FallbackEmissionFactorBatchValidator(var rules: MutableList<FallbackEmissionFactorBatchRule>) {
    fun validate(item: FallbackEmissionFactorBatch): MutableList<String> {
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

    fun requireValid(item: FallbackEmissionFactorBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackEmissionFactorBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackEmissionFactorBatchValidator(): FallbackEmissionFactorBatchValidator {
    return FallbackEmissionFactorBatchValidator(
        mutableListOf<FallbackEmissionFactorBatchRule>(
            FallbackEmissionFactorBatchNameLengthRule(1000),
            FallbackEmissionFactorBatchRequiredNameRule("fallback emission factor batch needs a value"),
            FallbackEmissionFactorBatchCodeLengthRule(16),
            FallbackEmissionFactorBatchCeilingRangeRule(6.7, 557.8),
            FallbackEmissionFactorBatchRequiredCodeRule("fallback emission factor batch needs a value"),
        ),
    )
}
