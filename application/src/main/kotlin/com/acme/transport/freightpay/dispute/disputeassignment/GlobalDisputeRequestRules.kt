package com.acme.transport.freightpay.dispute.disputeassignment

import com.acme.transport.kit.text.normalize

/** A rule a global dispute request must pass. Check returns its problems, or none. */
interface GlobalDisputeRequestRule {
    fun check(item: GlobalDisputeRequest): MutableList<String>
}

data class GlobalDisputeRequestRequiredDescriptionRule(var message: String) : GlobalDisputeRequestRule {
    override fun check(item: GlobalDisputeRequest): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalDisputeRequestDescriptionLengthRule(var maxLength: Int) : GlobalDisputeRequestRule {
    override fun check(item: GlobalDisputeRequest): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDisputeRequestAllowedChannelRule(var allowed: MutableList<String>) : GlobalDisputeRequestRule {
    override fun check(item: GlobalDisputeRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDisputeRequestMinimumRangeRule(var min: Double, var max: Double) : GlobalDisputeRequestRule {
    override fun check(item: GlobalDisputeRequest): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class GlobalDisputeRequestCodeLengthRule(var maxLength: Int) : GlobalDisputeRequestRule {
    override fun check(item: GlobalDisputeRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDisputeRequestRequiredNameRule(var message: String) : GlobalDisputeRequestRule {
    override fun check(item: GlobalDisputeRequest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global dispute request. */
data class GlobalDisputeRequestValidator(var rules: MutableList<GlobalDisputeRequestRule>) {
    fun validate(item: GlobalDisputeRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalDisputeRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalDisputeRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalDisputeRequestValidator(): GlobalDisputeRequestValidator {
    return GlobalDisputeRequestValidator(
        mutableListOf<GlobalDisputeRequestRule>(
            GlobalDisputeRequestRequiredDescriptionRule("global dispute request needs an entry"),
            GlobalDisputeRequestDescriptionLengthRule(32),
            GlobalDisputeRequestAllowedChannelRule(mutableListOf<String>("weighted")),
            GlobalDisputeRequestMinimumRangeRule(8.5, 1709.3),
            GlobalDisputeRequestCodeLengthRule(16),
            GlobalDisputeRequestRequiredNameRule("global dispute request needs a value"),
        ),
    )
}
