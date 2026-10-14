package com.acme.transport.customs.broker.tier

/** A rule a global broker request must pass. Check returns its problems, or none. */
interface GlobalBrokerRequestRule {
    fun check(item: GlobalBrokerRequest): MutableList<String>
}

data class GlobalBrokerRequestCostRangeRule(var min: Double, var max: Double) : GlobalBrokerRequestRule {
    override fun check(item: GlobalBrokerRequest): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class GlobalBrokerRequestRequiredLabelRule(var message: String) : GlobalBrokerRequestRule {
    override fun check(item: GlobalBrokerRequest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalBrokerRequestLabelLengthRule(var maxLength: Int) : GlobalBrokerRequestRule {
    override fun check(item: GlobalBrokerRequest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalBrokerRequestTimezoneLengthRule(var maxLength: Int) : GlobalBrokerRequestRule {
    override fun check(item: GlobalBrokerRequest): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalBrokerRequestRequiredTimezoneRule(var message: String) : GlobalBrokerRequestRule {
    override fun check(item: GlobalBrokerRequest): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalBrokerRequestCodePrefixRule(var prefix: String) : GlobalBrokerRequestRule {
    override fun check(item: GlobalBrokerRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global broker request. */
data class GlobalBrokerRequestValidator(var rules: MutableList<GlobalBrokerRequestRule>) {
    fun validate(item: GlobalBrokerRequest): MutableList<String> {
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

    fun requireValid(item: GlobalBrokerRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalBrokerRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalBrokerRequestValidator(): GlobalBrokerRequestValidator {
    return GlobalBrokerRequestValidator(
        mutableListOf<GlobalBrokerRequestRule>(
            GlobalBrokerRequestCostRangeRule(3.0, 4674.3),
            GlobalBrokerRequestRequiredLabelRule("global broker request needs a value"),
            GlobalBrokerRequestLabelLengthRule(255),
            GlobalBrokerRequestTimezoneLengthRule(80),
            GlobalBrokerRequestRequiredTimezoneRule("global broker request needs a value"),
            GlobalBrokerRequestCodePrefixRule("X"),
        ),
    )
}
