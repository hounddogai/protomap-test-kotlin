package com.acme.transport.freightpay.fuelsurcharge.window

/** A rule a default fuel surcharge window must pass. Check returns its problems, or none. */
interface DefaultFuelSurchargeWindowRule {
    fun check(item: DefaultFuelSurchargeWindow): MutableList<String>
}

data class DefaultFuelSurchargeWindowRequiredCodeRule(var message: String) : DefaultFuelSurchargeWindowRule {
    override fun check(item: DefaultFuelSurchargeWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultFuelSurchargeWindowRequiredCarrierRule(var message: String) : DefaultFuelSurchargeWindowRule {
    override fun check(item: DefaultFuelSurchargeWindow): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultFuelSurchargeWindowRequiredSegmentRule(var message: String) : DefaultFuelSurchargeWindowRule {
    override fun check(item: DefaultFuelSurchargeWindow): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultFuelSurchargeWindowCodePrefixRule(var prefix: String) : DefaultFuelSurchargeWindowRule {
    override fun check(item: DefaultFuelSurchargeWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultFuelSurchargeWindowCarrierLengthRule(var maxLength: Int) : DefaultFuelSurchargeWindowRule {
    override fun check(item: DefaultFuelSurchargeWindow): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultFuelSurchargeWindowRequiredLabelRule(var message: String) : DefaultFuelSurchargeWindowRule {
    override fun check(item: DefaultFuelSurchargeWindow): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default fuel surcharge window. */
data class DefaultFuelSurchargeWindowValidator(var rules: MutableList<DefaultFuelSurchargeWindowRule>) {
    fun validate(item: DefaultFuelSurchargeWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultFuelSurchargeWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultFuelSurchargeWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultFuelSurchargeWindowValidator(): DefaultFuelSurchargeWindowValidator {
    return DefaultFuelSurchargeWindowValidator(
        mutableListOf<DefaultFuelSurchargeWindowRule>(
            DefaultFuelSurchargeWindowRequiredCodeRule("default fuel surcharge window needs this field"),
            DefaultFuelSurchargeWindowRequiredCarrierRule("default fuel surcharge window needs an entry"),
            DefaultFuelSurchargeWindowRequiredSegmentRule("default fuel surcharge window needs a value"),
            DefaultFuelSurchargeWindowCodePrefixRule("OPS"),
            DefaultFuelSurchargeWindowCarrierLengthRule(1000),
            DefaultFuelSurchargeWindowRequiredLabelRule("default fuel surcharge window needs an entry"),
        ),
    )
}
