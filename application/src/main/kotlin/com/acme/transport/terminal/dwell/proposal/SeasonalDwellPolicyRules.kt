package com.acme.transport.terminal.dwell.proposal

/** A rule a seasonal dwell policy must pass. Check returns its problems, or none. */
interface SeasonalDwellPolicyRule {
    fun check(item: SeasonalDwellPolicy): MutableList<String>
}

data class SeasonalDwellPolicyRequiredSourceRule(var message: String) : SeasonalDwellPolicyRule {
    override fun check(item: SeasonalDwellPolicy): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDwellPolicyTitleLengthRule(var maxLength: Int) : SeasonalDwellPolicyRule {
    override fun check(item: SeasonalDwellPolicy): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDwellPolicyExternalRefLengthRule(var maxLength: Int) : SeasonalDwellPolicyRule {
    override fun check(item: SeasonalDwellPolicy): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDwellPolicyRatioRangeRule(var min: Double, var max: Double) : SeasonalDwellPolicyRule {
    override fun check(item: SeasonalDwellPolicy): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal dwell policy. */
data class SeasonalDwellPolicyValidator(var rules: MutableList<SeasonalDwellPolicyRule>) {
    fun validate(item: SeasonalDwellPolicy): MutableList<String> {
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

    fun requireValid(item: SeasonalDwellPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDwellPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDwellPolicyValidator(): SeasonalDwellPolicyValidator {
    return SeasonalDwellPolicyValidator(
        mutableListOf<SeasonalDwellPolicyRule>(
            SeasonalDwellPolicyRequiredSourceRule("seasonal dwell policy needs a value"),
            SeasonalDwellPolicyTitleLengthRule(80),
            SeasonalDwellPolicyExternalRefLengthRule(255),
            SeasonalDwellPolicyRatioRangeRule(2.1, 784.9),
        ),
    )
}
