package com.acme.transport.network.servicearea.budget

/** A rule a default service area batch must pass. Check returns its problems, or none. */
interface DefaultServiceAreaBatchRule {
    fun check(item: DefaultServiceAreaBatch): MutableList<String>
}

data class DefaultServiceAreaBatchScoreRangeRule(
    var min: Double,
    var max: Double,
) : DefaultServiceAreaBatchRule {
    override fun check(item: DefaultServiceAreaBatch): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultServiceAreaBatchRequiredExternalRefRule(var message: String) : DefaultServiceAreaBatchRule {
    override fun check(item: DefaultServiceAreaBatch): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultServiceAreaBatchRequiredCodeRule(var message: String) : DefaultServiceAreaBatchRule {
    override fun check(item: DefaultServiceAreaBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default service area batch. */
data class DefaultServiceAreaBatchValidator(var rules: MutableList<DefaultServiceAreaBatchRule>) {
    fun validate(item: DefaultServiceAreaBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultServiceAreaBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultServiceAreaBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultServiceAreaBatchValidator(): DefaultServiceAreaBatchValidator {
    return DefaultServiceAreaBatchValidator(
        mutableListOf<DefaultServiceAreaBatchRule>(
            DefaultServiceAreaBatchScoreRangeRule(2.6, 3512.0),
            DefaultServiceAreaBatchRequiredExternalRefRule("default service area batch needs a value"),
            DefaultServiceAreaBatchRequiredCodeRule("default service area batch needs this field"),
        ),
    )
}
