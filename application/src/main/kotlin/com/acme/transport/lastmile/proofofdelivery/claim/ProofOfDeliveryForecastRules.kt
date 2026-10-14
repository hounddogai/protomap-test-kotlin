package com.acme.transport.lastmile.proofofdelivery.claim

/** A rule a proof of delivery forecast must pass. Check returns its problems, or none. */
interface ProofOfDeliveryForecastRule {
    fun check(item: ProofOfDeliveryForecast): MutableList<String>
}

data class ProofOfDeliveryForecastCodeLengthRule(var maxLength: Int) : ProofOfDeliveryForecastRule {
    override fun check(item: ProofOfDeliveryForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryForecastVehicleIdLengthRule(var maxLength: Int) : ProofOfDeliveryForecastRule {
    override fun check(item: ProofOfDeliveryForecast): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryForecastRequiredDescriptionRule(var message: String) : ProofOfDeliveryForecastRule {
    override fun check(item: ProofOfDeliveryForecast): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryForecastCodePrefixRule(var prefix: String) : ProofOfDeliveryForecastRule {
    override fun check(item: ProofOfDeliveryForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery forecast. */
data class ProofOfDeliveryForecastValidator(var rules: MutableList<ProofOfDeliveryForecastRule>) {
    fun validate(item: ProofOfDeliveryForecast): MutableList<String> {
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

    fun requireValid(item: ProofOfDeliveryForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryForecastValidator(): ProofOfDeliveryForecastValidator {
    return ProofOfDeliveryForecastValidator(
        mutableListOf<ProofOfDeliveryForecastRule>(
            ProofOfDeliveryForecastCodeLengthRule(3600),
            ProofOfDeliveryForecastVehicleIdLengthRule(64),
            ProofOfDeliveryForecastRequiredDescriptionRule("proof of delivery forecast needs a value"),
            ProofOfDeliveryForecastCodePrefixRule("OPS"),
        ),
    )
}
