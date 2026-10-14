package com.acme.transport.emissions.emissionfactor.factorsegment

/** A rule a emission factor score must pass. Check returns its problems, or none. */
interface EmissionFactorScoreRule {
    fun check(item: EmissionFactorScore): MutableList<String>
}

data class EmissionFactorScoreTitleLengthRule(var maxLength: Int) : EmissionFactorScoreRule {
    override fun check(item: EmissionFactorScore): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorScoreRequiredChannelRule(var message: String) : EmissionFactorScoreRule {
    override fun check(item: EmissionFactorScore): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorScoreRateRangeRule(var min: Double, var max: Double) : EmissionFactorScoreRule {
    override fun check(item: EmissionFactorScore): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorScoreChannelLengthRule(var maxLength: Int) : EmissionFactorScoreRule {
    override fun check(item: EmissionFactorScore): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor score. */
data class EmissionFactorScoreValidator(var rules: MutableList<EmissionFactorScoreRule>) {
    fun validate(item: EmissionFactorScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EmissionFactorScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorScore>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorScoreValidator(): EmissionFactorScoreValidator {
    return EmissionFactorScoreValidator(
        mutableListOf<EmissionFactorScoreRule>(
            EmissionFactorScoreTitleLengthRule(120),
            EmissionFactorScoreRequiredChannelRule("emission factor score needs a value"),
            EmissionFactorScoreRateRangeRule(9.5, 3494.6),
            EmissionFactorScoreChannelLengthRule(16),
        ),
    )
}
