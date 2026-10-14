package com.acme.transport.emissions.emissionfactor.segment

/** A rule a default emission factor batch must pass. Check returns its problems, or none. */
interface DefaultEmissionFactorBatchRule {
    fun check(item: DefaultEmissionFactorBatch): MutableList<String>
}

data class DefaultEmissionFactorBatchRequiredCodeRule(var message: String) : DefaultEmissionFactorBatchRule {
    override fun check(item: DefaultEmissionFactorBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultEmissionFactorBatchFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : DefaultEmissionFactorBatchRule {
    override fun check(item: DefaultEmissionFactorBatch): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultEmissionFactorBatchCodePrefixRule(var prefix: String) : DefaultEmissionFactorBatchRule {
    override fun check(item: DefaultEmissionFactorBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default emission factor batch. */
data class DefaultEmissionFactorBatchValidator(var rules: MutableList<DefaultEmissionFactorBatchRule>) {
    fun validate(item: DefaultEmissionFactorBatch): MutableList<String> {
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

    fun requireValid(item: DefaultEmissionFactorBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultEmissionFactorBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultEmissionFactorBatchValidator(): DefaultEmissionFactorBatchValidator {
    return DefaultEmissionFactorBatchValidator(
        mutableListOf<DefaultEmissionFactorBatchRule>(
            DefaultEmissionFactorBatchRequiredCodeRule("default emission factor batch needs an entry"),
            DefaultEmissionFactorBatchFuelLitersRangeRule(4.8, 3451.0),
            DefaultEmissionFactorBatchCodePrefixRule("ACM"),
        ),
    )
}
