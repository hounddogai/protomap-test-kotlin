package com.acme.transport.linehaul.relay.claim

/** A rule a seasonal relay request must pass. Check returns its problems, or none. */
interface SeasonalRelayRequestRule {
    fun check(item: SeasonalRelayRequest): MutableList<String>
}

data class SeasonalRelayRequestLabelLengthRule(var maxLength: Int) : SeasonalRelayRequestRule {
    override fun check(item: SeasonalRelayRequest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRelayRequestCodePrefixRule(var prefix: String) : SeasonalRelayRequestRule {
    override fun check(item: SeasonalRelayRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRelayRequestChannelLengthRule(var maxLength: Int) : SeasonalRelayRequestRule {
    override fun check(item: SeasonalRelayRequest): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRelayRequestRateRangeRule(var min: Double, var max: Double) : SeasonalRelayRequestRule {
    override fun check(item: SeasonalRelayRequest): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal relay request. */
data class SeasonalRelayRequestValidator(var rules: MutableList<SeasonalRelayRequestRule>) {
    fun validate(item: SeasonalRelayRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalRelayRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRelayRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRelayRequestValidator(): SeasonalRelayRequestValidator {
    return SeasonalRelayRequestValidator(
        mutableListOf<SeasonalRelayRequestRule>(
            SeasonalRelayRequestLabelLengthRule(1000),
            SeasonalRelayRequestCodePrefixRule("X"),
            SeasonalRelayRequestChannelLengthRule(32),
            SeasonalRelayRequestRateRangeRule(9.9, 1529.1),
        ),
    )
}
