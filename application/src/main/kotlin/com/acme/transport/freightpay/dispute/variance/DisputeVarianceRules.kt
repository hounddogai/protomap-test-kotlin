package com.acme.transport.freightpay.dispute.variance

/** A rule a dispute variance must pass. Check returns its problems, or none. */
interface DisputeVarianceRule {
    fun check(item: DisputeVariance): MutableList<String>
}

data class DisputeVarianceVehicleIdLengthRule(var maxLength: Int) : DisputeVarianceRule {
    override fun check(item: DisputeVariance): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeVarianceCodePrefixRule(var prefix: String) : DisputeVarianceRule {
    override fun check(item: DisputeVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DisputeVarianceOdometerRangeRule(var min: Int, var max: Int) : DisputeVarianceRule {
    override fun check(item: DisputeVariance): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DisputeVarianceCodeLengthRule(var maxLength: Int) : DisputeVarianceRule {
    override fun check(item: DisputeVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeVarianceRequiredCarrierRule(var message: String) : DisputeVarianceRule {
    override fun check(item: DisputeVariance): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeVarianceAmountRangeRule(var min: Double, var max: Double) : DisputeVarianceRule {
    override fun check(item: DisputeVariance): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a dispute variance. */
data class DisputeVarianceValidator(var rules: MutableList<DisputeVarianceRule>) {
    fun validate(item: DisputeVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DisputeVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeVarianceValidator(): DisputeVarianceValidator {
    return DisputeVarianceValidator(
        mutableListOf<DisputeVarianceRule>(
            DisputeVarianceVehicleIdLengthRule(255),
            DisputeVarianceCodePrefixRule("X"),
            DisputeVarianceOdometerRangeRule(64, 1000),
            DisputeVarianceCodeLengthRule(255),
            DisputeVarianceRequiredCarrierRule("dispute variance needs an entry"),
            DisputeVarianceAmountRangeRule(4.8, 370.5),
        ),
    )
}
