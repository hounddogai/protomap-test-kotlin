package com.acme.transport.freightpay.freightinvoice.budget

/** A rule a freight invoice forecast must pass. Check returns its problems, or none. */
interface FreightInvoiceForecastRule {
    fun check(item: FreightInvoiceForecast): MutableList<String>
}

data class FreightInvoiceForecastRequiredContactEmailRule(var message: String) : FreightInvoiceForecastRule {
    override fun check(item: FreightInvoiceForecast): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceForecastRequiredNameRule(var message: String) : FreightInvoiceForecastRule {
    override fun check(item: FreightInvoiceForecast): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceForecastRequiredCodeRule(var message: String) : FreightInvoiceForecastRule {
    override fun check(item: FreightInvoiceForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceForecastContactEmailLengthRule(var maxLength: Int) : FreightInvoiceForecastRule {
    override fun check(item: FreightInvoiceForecast): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceForecastCodeLengthRule(var maxLength: Int) : FreightInvoiceForecastRule {
    override fun check(item: FreightInvoiceForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceForecastEndsAtRangeRule(var min: Int, var max: Int) : FreightInvoiceForecastRule {
    override fun check(item: FreightInvoiceForecast): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a freight invoice forecast. */
data class FreightInvoiceForecastValidator(var rules: MutableList<FreightInvoiceForecastRule>) {
    fun validate(item: FreightInvoiceForecast): MutableList<String> {
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

    fun requireValid(item: FreightInvoiceForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceForecastValidator(): FreightInvoiceForecastValidator {
    return FreightInvoiceForecastValidator(
        mutableListOf<FreightInvoiceForecastRule>(
            FreightInvoiceForecastRequiredContactEmailRule("freight invoice forecast needs a value"),
            FreightInvoiceForecastRequiredNameRule("freight invoice forecast needs an entry"),
            FreightInvoiceForecastRequiredCodeRule("freight invoice forecast needs an entry"),
            FreightInvoiceForecastContactEmailLengthRule(255),
            FreightInvoiceForecastCodeLengthRule(32),
            FreightInvoiceForecastEndsAtRangeRule(64, 3600),
        ),
    )
}
