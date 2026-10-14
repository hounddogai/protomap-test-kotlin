package com.acme.transport.freightpay.accessorial.accessorialadjustment

/** A rule a adaptive accessorial adjustment must pass. Check returns its problems, or none. */
interface AdaptiveAccessorialAdjustmentRule {
    fun check(item: AdaptiveAccessorialAdjustment): MutableList<String>
}

data class AdaptiveAccessorialAdjustmentLabelLengthRule(
    var maxLength: Int,
) : AdaptiveAccessorialAdjustmentRule {
    override fun check(item: AdaptiveAccessorialAdjustment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAccessorialAdjustmentVehicleIdLengthRule(
    var maxLength: Int,
) : AdaptiveAccessorialAdjustmentRule {
    override fun check(item: AdaptiveAccessorialAdjustment): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAccessorialAdjustmentRequiredCountryRule(
    var message: String,
) : AdaptiveAccessorialAdjustmentRule {
    override fun check(item: AdaptiveAccessorialAdjustment): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAccessorialAdjustmentCountryLengthRule(
    var maxLength: Int,
) : AdaptiveAccessorialAdjustmentRule {
    override fun check(item: AdaptiveAccessorialAdjustment): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAccessorialAdjustmentRequiredCodeRule(
    var message: String,
) : AdaptiveAccessorialAdjustmentRule {
    override fun check(item: AdaptiveAccessorialAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive accessorial adjustment. */
data class AdaptiveAccessorialAdjustmentValidator(var rules: MutableList<AdaptiveAccessorialAdjustmentRule>) {
    fun validate(item: AdaptiveAccessorialAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveAccessorialAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveAccessorialAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveAccessorialAdjustmentValidator(): AdaptiveAccessorialAdjustmentValidator {
    return AdaptiveAccessorialAdjustmentValidator(
        mutableListOf<AdaptiveAccessorialAdjustmentRule>(
            AdaptiveAccessorialAdjustmentLabelLengthRule(64),
            AdaptiveAccessorialAdjustmentVehicleIdLengthRule(80),
            AdaptiveAccessorialAdjustmentRequiredCountryRule("adaptive accessorial adjustment needs a value"),
            AdaptiveAccessorialAdjustmentCountryLengthRule(3600),
            AdaptiveAccessorialAdjustmentRequiredCodeRule("adaptive accessorial adjustment needs a value"),
        ),
    )
}
