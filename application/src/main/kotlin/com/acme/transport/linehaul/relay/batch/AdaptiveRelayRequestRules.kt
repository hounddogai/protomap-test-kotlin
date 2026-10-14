package com.acme.transport.linehaul.relay.batch

/** A rule a adaptive relay request must pass. Check returns its problems, or none. */
interface AdaptiveRelayRequestRule {
    fun check(item: AdaptiveRelayRequest): MutableList<String>
}

data class AdaptiveRelayRequestSequenceRangeRule(var min: Int, var max: Int) : AdaptiveRelayRequestRule {
    override fun check(item: AdaptiveRelayRequest): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRelayRequestRequiredNameRule(var message: String) : AdaptiveRelayRequestRule {
    override fun check(item: AdaptiveRelayRequest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRelayRequestActiveGuardRule(var limit: Int) : AdaptiveRelayRequestRule {
    override fun check(item: AdaptiveRelayRequest): MutableList<String> {
        if (item.active && item.startsAt > limit) {
            return mutableListOf<String>("starts at ${item.startsAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRelayRequestCurrencyLengthRule(var maxLength: Int) : AdaptiveRelayRequestRule {
    override fun check(item: AdaptiveRelayRequest): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive relay request. */
data class AdaptiveRelayRequestValidator(var rules: MutableList<AdaptiveRelayRequestRule>) {
    fun validate(item: AdaptiveRelayRequest): MutableList<String> {
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

    fun requireValid(item: AdaptiveRelayRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveRelayRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveRelayRequestValidator(): AdaptiveRelayRequestValidator {
    return AdaptiveRelayRequestValidator(
        mutableListOf<AdaptiveRelayRequestRule>(
            AdaptiveRelayRequestSequenceRangeRule(255, 1000),
            AdaptiveRelayRequestRequiredNameRule("adaptive relay request needs this field"),
            AdaptiveRelayRequestActiveGuardRule(120),
            AdaptiveRelayRequestCurrencyLengthRule(255),
        ),
    )
}
