package com.acme.transport.network.hub.allocation

/** A rule a seasonal hub claim must pass. Check returns its problems, or none. */
interface SeasonalHubClaimRule {
    fun check(item: SeasonalHubClaim): MutableList<String>
}

data class SeasonalHubClaimTitleLengthRule(var maxLength: Int) : SeasonalHubClaimRule {
    override fun check(item: SeasonalHubClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalHubClaimRequiredChannelRule(var message: String) : SeasonalHubClaimRule {
    override fun check(item: SeasonalHubClaim): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalHubClaimAmountRangeRule(var min: Double, var max: Double) : SeasonalHubClaimRule {
    override fun check(item: SeasonalHubClaim): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalHubClaimChannelLengthRule(var maxLength: Int) : SeasonalHubClaimRule {
    override fun check(item: SeasonalHubClaim): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal hub claim. */
data class SeasonalHubClaimValidator(var rules: MutableList<SeasonalHubClaimRule>) {
    fun validate(item: SeasonalHubClaim): MutableList<String> {
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

    fun requireValid(item: SeasonalHubClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalHubClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalHubClaimValidator(): SeasonalHubClaimValidator {
    return SeasonalHubClaimValidator(
        mutableListOf<SeasonalHubClaimRule>(
            SeasonalHubClaimTitleLengthRule(16),
            SeasonalHubClaimRequiredChannelRule("seasonal hub claim needs this field"),
            SeasonalHubClaimAmountRangeRule(3.4, 987.6),
            SeasonalHubClaimChannelLengthRule(80),
        ),
    )
}
