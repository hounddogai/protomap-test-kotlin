package com.acme.transport.safety.violation.batch

/** A rule a weighted violation request must pass. Check returns its problems, or none. */
interface WeightedViolationRequestRule {
    fun check(item: WeightedViolationRequest): MutableList<String>
}

data class WeightedViolationRequestCodeLengthRule(var maxLength: Int) : WeightedViolationRequestRule {
    override fun check(item: WeightedViolationRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedViolationRequestRequiredVehicleIdRule(var message: String) : WeightedViolationRequestRule {
    override fun check(item: WeightedViolationRequest): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedViolationRequestRequiredLabelRule(var message: String) : WeightedViolationRequestRule {
    override fun check(item: WeightedViolationRequest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedViolationRequestVehicleIdLengthRule(var maxLength: Int) : WeightedViolationRequestRule {
    override fun check(item: WeightedViolationRequest): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedViolationRequestRequiredCodeRule(var message: String) : WeightedViolationRequestRule {
    override fun check(item: WeightedViolationRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted violation request. */
data class WeightedViolationRequestValidator(var rules: MutableList<WeightedViolationRequestRule>) {
    fun validate(item: WeightedViolationRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedViolationRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedViolationRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedViolationRequestValidator(): WeightedViolationRequestValidator {
    return WeightedViolationRequestValidator(
        mutableListOf<WeightedViolationRequestRule>(
            WeightedViolationRequestCodeLengthRule(64),
            WeightedViolationRequestRequiredVehicleIdRule("weighted violation request needs an entry"),
            WeightedViolationRequestRequiredLabelRule("weighted violation request needs a value"),
            WeightedViolationRequestVehicleIdLengthRule(86400),
            WeightedViolationRequestRequiredCodeRule("weighted violation request needs an entry"),
        ),
    )
}
