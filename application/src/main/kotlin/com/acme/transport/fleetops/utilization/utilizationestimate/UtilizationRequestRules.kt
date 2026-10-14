package com.acme.transport.fleetops.utilization.utilizationestimate

/** A rule a utilization request must pass. Check returns its problems, or none. */
interface UtilizationRequestRule {
    fun check(item: UtilizationRequest): MutableList<String>
}

data class UtilizationRequestMinimumRangeRule(var min: Double, var max: Double) : UtilizationRequestRule {
    override fun check(item: UtilizationRequest): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationRequestContactEmailLengthRule(var maxLength: Int) : UtilizationRequestRule {
    override fun check(item: UtilizationRequest): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationRequestRequiredCarrierRule(var message: String) : UtilizationRequestRule {
    override fun check(item: UtilizationRequest): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization request. */
data class UtilizationRequestValidator(var rules: MutableList<UtilizationRequestRule>) {
    fun validate(item: UtilizationRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationRequestValidator(): UtilizationRequestValidator {
    return UtilizationRequestValidator(
        mutableListOf<UtilizationRequestRule>(
            UtilizationRequestMinimumRangeRule(8.6, 1389.5),
            UtilizationRequestContactEmailLengthRule(3600),
            UtilizationRequestRequiredCarrierRule("utilization request needs this field"),
        ),
    )
}
