package com.acme.transport.freightpay.freightinvoice.invoiceadjustment

/** A rule a rolling freight invoice request must pass. Check returns its problems, or none. */
interface RollingFreightInvoiceRequestRule {
    fun check(item: RollingFreightInvoiceRequest): MutableList<String>
}

data class RollingFreightInvoiceRequestRequiredTitleRule(
    var message: String,
) : RollingFreightInvoiceRequestRule {
    override fun check(item: RollingFreightInvoiceRequest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingFreightInvoiceRequestCodePrefixRule(var prefix: String) : RollingFreightInvoiceRequestRule {
    override fun check(item: RollingFreightInvoiceRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingFreightInvoiceRequestCodeLengthRule(var maxLength: Int) : RollingFreightInvoiceRequestRule {
    override fun check(item: RollingFreightInvoiceRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingFreightInvoiceRequestOdometerRangeRule(
    var min: Int,
    var max: Int,
) : RollingFreightInvoiceRequestRule {
    override fun check(item: RollingFreightInvoiceRequest): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class RollingFreightInvoiceRequestThresholdRangeRule(
    var min: Double,
    var max: Double,
) : RollingFreightInvoiceRequestRule {
    override fun check(item: RollingFreightInvoiceRequest): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingFreightInvoiceRequestLaneLengthRule(var maxLength: Int) : RollingFreightInvoiceRequestRule {
    override fun check(item: RollingFreightInvoiceRequest): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling freight invoice request. */
data class RollingFreightInvoiceRequestValidator(var rules: MutableList<RollingFreightInvoiceRequestRule>) {
    fun validate(item: RollingFreightInvoiceRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingFreightInvoiceRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingFreightInvoiceRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingFreightInvoiceRequestValidator(): RollingFreightInvoiceRequestValidator {
    return RollingFreightInvoiceRequestValidator(
        mutableListOf<RollingFreightInvoiceRequestRule>(
            RollingFreightInvoiceRequestRequiredTitleRule("rolling freight invoice request needs an entry"),
            RollingFreightInvoiceRequestCodePrefixRule("REF"),
            RollingFreightInvoiceRequestCodeLengthRule(80),
            RollingFreightInvoiceRequestOdometerRangeRule(32, 255),
            RollingFreightInvoiceRequestThresholdRangeRule(8.1, 3108.0),
            RollingFreightInvoiceRequestLaneLengthRule(255),
        ),
    )
}
