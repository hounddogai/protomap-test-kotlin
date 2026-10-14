package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

/** A rule a fuel surcharge forecast must pass. Check returns its problems, or none. */
interface FuelSurchargeForecastRule {
    fun check(item: FuelSurchargeForecast): MutableList<String>
}

data class FuelSurchargeForecastCodePrefixRule(var prefix: String) : FuelSurchargeForecastRule {
    override fun check(item: FuelSurchargeForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeForecastRequiredPhoneRule(var message: String) : FuelSurchargeForecastRule {
    override fun check(item: FuelSurchargeForecast): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeForecastDepotLengthRule(var maxLength: Int) : FuelSurchargeForecastRule {
    override fun check(item: FuelSurchargeForecast): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeForecastRequiredNameRule(var message: String) : FuelSurchargeForecastRule {
    override fun check(item: FuelSurchargeForecast): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge forecast. */
data class FuelSurchargeForecastValidator(var rules: MutableList<FuelSurchargeForecastRule>) {
    fun validate(item: FuelSurchargeForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelSurchargeForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeForecastValidator(): FuelSurchargeForecastValidator {
    return FuelSurchargeForecastValidator(
        mutableListOf<FuelSurchargeForecastRule>(
            FuelSurchargeForecastCodePrefixRule("OPS"),
            FuelSurchargeForecastRequiredPhoneRule("fuel surcharge forecast needs an entry"),
            FuelSurchargeForecastDepotLengthRule(86400),
            FuelSurchargeForecastRequiredNameRule("fuel surcharge forecast needs this field"),
        ),
    )
}
