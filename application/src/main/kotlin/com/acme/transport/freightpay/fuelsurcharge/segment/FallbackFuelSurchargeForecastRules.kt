package com.acme.transport.freightpay.fuelsurcharge.segment

/** A rule a fallback fuel surcharge forecast must pass. Check returns its problems, or none. */
interface FallbackFuelSurchargeForecastRule {
    fun check(item: FallbackFuelSurchargeForecast): MutableList<String>
}

data class FallbackFuelSurchargeForecastRequiredExternalRefRule(
    var message: String,
) : FallbackFuelSurchargeForecastRule {
    override fun check(item: FallbackFuelSurchargeForecast): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackFuelSurchargeForecastScoreRangeRule(
    var min: Double,
    var max: Double,
) : FallbackFuelSurchargeForecastRule {
    override fun check(item: FallbackFuelSurchargeForecast): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackFuelSurchargeForecastRequiredCodeRule(
    var message: String,
) : FallbackFuelSurchargeForecastRule {
    override fun check(item: FallbackFuelSurchargeForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackFuelSurchargeForecastEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : FallbackFuelSurchargeForecastRule {
    override fun check(item: FallbackFuelSurchargeForecast): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackFuelSurchargeForecastCodePrefixRule(var prefix: String) : FallbackFuelSurchargeForecastRule {
    override fun check(item: FallbackFuelSurchargeForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback fuel surcharge forecast. */
data class FallbackFuelSurchargeForecastValidator(var rules: MutableList<FallbackFuelSurchargeForecastRule>) {
    fun validate(item: FallbackFuelSurchargeForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackFuelSurchargeForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackFuelSurchargeForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackFuelSurchargeForecastValidator(): FallbackFuelSurchargeForecastValidator {
    return FallbackFuelSurchargeForecastValidator(
        mutableListOf<FallbackFuelSurchargeForecastRule>(
            FallbackFuelSurchargeForecastRequiredExternalRefRule(
                "fallback fuel surcharge forecast needs this field",
            ),
            FallbackFuelSurchargeForecastScoreRangeRule(3.5, 1153.0),
            FallbackFuelSurchargeForecastRequiredCodeRule("fallback fuel surcharge forecast needs this field"),
            FallbackFuelSurchargeForecastEndsAtRangeRule(32, 3600),
            FallbackFuelSurchargeForecastCodePrefixRule("OPS"),
        ),
    )
}
