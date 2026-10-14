package com.acme.transport.freightpay.dispute.window

/** A rule a dispute request must pass. Check returns its problems, or none. */
interface DisputeRequestRule {
    fun check(item: DisputeRequest): MutableList<String>
}

data class DisputeRequestRateRangeRule(var min: Double, var max: Double) : DisputeRequestRule {
    override fun check(item: DisputeRequest): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class DisputeRequestDriverIdLengthRule(var maxLength: Int) : DisputeRequestRule {
    override fun check(item: DisputeRequest): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeRequestRevisionRangeRule(var min: Int, var max: Int) : DisputeRequestRule {
    override fun check(item: DisputeRequest): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a dispute request. */
data class DisputeRequestValidator(var rules: MutableList<DisputeRequestRule>) {
    fun validate(item: DisputeRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DisputeRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeRequestValidator(): DisputeRequestValidator {
    return DisputeRequestValidator(
        mutableListOf<DisputeRequestRule>(
            DisputeRequestRateRangeRule(7.8, 3633.1),
            DisputeRequestDriverIdLengthRule(80),
            DisputeRequestRevisionRangeRule(16, 64),
        ),
    )
}
