package com.acme.transport.lastmile.proofofdelivery.rollup

/** A rule a proof of delivery request must pass. Check returns its problems, or none. */
interface ProofOfDeliveryRequestRule {
    fun check(item: ProofOfDeliveryRequest): MutableList<String>
}

data class ProofOfDeliveryRequestDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : ProofOfDeliveryRequestRule {
    override fun check(item: ProofOfDeliveryRequest): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryRequestRequiredRouteCodeRule(var message: String) : ProofOfDeliveryRequestRule {
    override fun check(item: ProofOfDeliveryRequest): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryRequestGraceDaysRangeRule(var min: Int, var max: Int) : ProofOfDeliveryRequestRule {
    override fun check(item: ProofOfDeliveryRequest): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a proof of delivery request. */
data class ProofOfDeliveryRequestValidator(var rules: MutableList<ProofOfDeliveryRequestRule>) {
    fun validate(item: ProofOfDeliveryRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ProofOfDeliveryRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryRequestValidator(): ProofOfDeliveryRequestValidator {
    return ProofOfDeliveryRequestValidator(
        mutableListOf<ProofOfDeliveryRequestRule>(
            ProofOfDeliveryRequestDurationMinutesRangeRule(120, 3600),
            ProofOfDeliveryRequestRequiredRouteCodeRule("proof of delivery request needs a value"),
            ProofOfDeliveryRequestGraceDaysRangeRule(16, 86400),
        ),
    )
}
