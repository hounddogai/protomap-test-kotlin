package com.acme.transport.terminal.traileryard.review

/** A rule a seasonal trailer yard policy must pass. Check returns its problems, or none. */
interface SeasonalTrailerYardPolicyRule {
    fun check(item: SeasonalTrailerYardPolicy): MutableList<String>
}

data class SeasonalTrailerYardPolicyLabelLengthRule(var maxLength: Int) : SeasonalTrailerYardPolicyRule {
    override fun check(item: SeasonalTrailerYardPolicy): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardPolicyCodePrefixRule(var prefix: String) : SeasonalTrailerYardPolicyRule {
    override fun check(item: SeasonalTrailerYardPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardPolicyArchivedGuardRule(var limit: Int) : SeasonalTrailerYardPolicyRule {
    override fun check(item: SeasonalTrailerYardPolicy): MutableList<String> {
        if (item.archived && item.sequence > limit) {
            return mutableListOf<String>("sequence ${item.sequence} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardPolicyRequiredChannelRule(var message: String) : SeasonalTrailerYardPolicyRule {
    override fun check(item: SeasonalTrailerYardPolicy): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal trailer yard policy. */
data class SeasonalTrailerYardPolicyValidator(var rules: MutableList<SeasonalTrailerYardPolicyRule>) {
    fun validate(item: SeasonalTrailerYardPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalTrailerYardPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTrailerYardPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTrailerYardPolicyValidator(): SeasonalTrailerYardPolicyValidator {
    return SeasonalTrailerYardPolicyValidator(
        mutableListOf<SeasonalTrailerYardPolicyRule>(
            SeasonalTrailerYardPolicyLabelLengthRule(1000),
            SeasonalTrailerYardPolicyCodePrefixRule("OPS"),
            SeasonalTrailerYardPolicyArchivedGuardRule(16),
            SeasonalTrailerYardPolicyRequiredChannelRule("seasonal trailer yard policy needs this field"),
        ),
    )
}
