package com.acme.transport.linehaul.relay.variance

/** A rule a default relay variance must pass. Check returns its problems, or none. */
interface DefaultRelayVarianceRule {
    fun check(item: DefaultRelayVariance): MutableList<String>
}

data class DefaultRelayVarianceCodeLengthRule(var maxLength: Int) : DefaultRelayVarianceRule {
    override fun check(item: DefaultRelayVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRelayVarianceRequiredCodeRule(var message: String) : DefaultRelayVarianceRule {
    override fun check(item: DefaultRelayVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRelayVarianceCodePrefixRule(var prefix: String) : DefaultRelayVarianceRule {
    override fun check(item: DefaultRelayVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRelayVarianceQuantityRangeRule(var min: Int, var max: Int) : DefaultRelayVarianceRule {
    override fun check(item: DefaultRelayVariance): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRelayVarianceChannelLengthRule(var maxLength: Int) : DefaultRelayVarianceRule {
    override fun check(item: DefaultRelayVariance): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default relay variance. */
data class DefaultRelayVarianceValidator(var rules: MutableList<DefaultRelayVarianceRule>) {
    fun validate(item: DefaultRelayVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultRelayVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRelayVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRelayVarianceValidator(): DefaultRelayVarianceValidator {
    return DefaultRelayVarianceValidator(
        mutableListOf<DefaultRelayVarianceRule>(
            DefaultRelayVarianceCodeLengthRule(64),
            DefaultRelayVarianceRequiredCodeRule("default relay variance needs this field"),
            DefaultRelayVarianceCodePrefixRule("REF"),
            DefaultRelayVarianceQuantityRangeRule(32, 1000),
            DefaultRelayVarianceChannelLengthRule(64),
        ),
    )
}
