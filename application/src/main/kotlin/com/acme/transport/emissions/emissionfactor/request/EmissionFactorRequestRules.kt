package com.acme.transport.emissions.emissionfactor.request

/** A rule a emission factor request must pass. Check returns its problems, or none. */
interface EmissionFactorRequestRule {
    fun check(item: EmissionFactorRequest): MutableList<String>
}

data class EmissionFactorRequestBatchSizeRangeRule(var min: Int, var max: Int) : EmissionFactorRequestRule {
    override fun check(item: EmissionFactorRequest): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorRequestEtaMinutesRangeRule(var min: Int, var max: Int) : EmissionFactorRequestRule {
    override fun check(item: EmissionFactorRequest): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class EmissionFactorRequestCodeLengthRule(var maxLength: Int) : EmissionFactorRequestRule {
    override fun check(item: EmissionFactorRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor request. */
data class EmissionFactorRequestValidator(var rules: MutableList<EmissionFactorRequestRule>) {
    fun validate(item: EmissionFactorRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EmissionFactorRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorRequestValidator(): EmissionFactorRequestValidator {
    return EmissionFactorRequestValidator(
        mutableListOf<EmissionFactorRequestRule>(
            EmissionFactorRequestBatchSizeRangeRule(32, 255),
            EmissionFactorRequestEtaMinutesRangeRule(64, 80),
            EmissionFactorRequestCodeLengthRule(16),
        ),
    )
}
