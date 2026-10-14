package com.acme.transport.freightpay.fuelsurcharge.milestone

/** A rule a global fuel surcharge forecast must pass. Check returns its problems, or none. */
interface GlobalFuelSurchargeForecastRule {
    fun check(item: GlobalFuelSurchargeForecast): MutableList<String>
}

data class GlobalFuelSurchargeForecastCapacityRangeRule(
    var min: Int,
    var max: Int,
) : GlobalFuelSurchargeForecastRule {
    override fun check(item: GlobalFuelSurchargeForecast): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class GlobalFuelSurchargeForecastEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : GlobalFuelSurchargeForecastRule {
    override fun check(item: GlobalFuelSurchargeForecast): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class GlobalFuelSurchargeForecastRequiredDockRule(var message: String) : GlobalFuelSurchargeForecastRule {
    override fun check(item: GlobalFuelSurchargeForecast): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelSurchargeForecastRevisionRangeRule(
    var min: Int,
    var max: Int,
) : GlobalFuelSurchargeForecastRule {
    override fun check(item: GlobalFuelSurchargeForecast): MutableList<String> {
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

data class GlobalFuelSurchargeForecastLabelLengthRule(var maxLength: Int) : GlobalFuelSurchargeForecastRule {
    override fun check(item: GlobalFuelSurchargeForecast): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelSurchargeForecastRequiredSourceRule(
    var message: String,
) : GlobalFuelSurchargeForecastRule {
    override fun check(item: GlobalFuelSurchargeForecast): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global fuel surcharge forecast. */
data class GlobalFuelSurchargeForecastValidator(var rules: MutableList<GlobalFuelSurchargeForecastRule>) {
    fun validate(item: GlobalFuelSurchargeForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalFuelSurchargeForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalFuelSurchargeForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalFuelSurchargeForecastValidator(): GlobalFuelSurchargeForecastValidator {
    return GlobalFuelSurchargeForecastValidator(
        mutableListOf<GlobalFuelSurchargeForecastRule>(
            GlobalFuelSurchargeForecastCapacityRangeRule(120, 1000),
            GlobalFuelSurchargeForecastEtaMinutesRangeRule(16, 80),
            GlobalFuelSurchargeForecastRequiredDockRule("global fuel surcharge forecast needs an entry"),
            GlobalFuelSurchargeForecastRevisionRangeRule(16, 255),
            GlobalFuelSurchargeForecastLabelLengthRule(32),
            GlobalFuelSurchargeForecastRequiredSourceRule("global fuel surcharge forecast needs an entry"),
        ),
    )
}
