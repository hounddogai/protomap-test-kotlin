package com.acme.transport.freightpay.fuelsurcharge.mapping

/** A rule a global fuel surcharge exception must pass. Check returns its problems, or none. */
interface GlobalFuelSurchargeExceptionRule {
    fun check(item: GlobalFuelSurchargeException): MutableList<String>
}

data class GlobalFuelSurchargeExceptionCodeLengthRule(var maxLength: Int) : GlobalFuelSurchargeExceptionRule {
    override fun check(item: GlobalFuelSurchargeException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelSurchargeExceptionVehicleIdLengthRule(
    var maxLength: Int,
) : GlobalFuelSurchargeExceptionRule {
    override fun check(item: GlobalFuelSurchargeException): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelSurchargeExceptionRequiredNameRule(
    var message: String,
) : GlobalFuelSurchargeExceptionRule {
    override fun check(item: GlobalFuelSurchargeException): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelSurchargeExceptionRequiredVehicleIdRule(
    var message: String,
) : GlobalFuelSurchargeExceptionRule {
    override fun check(item: GlobalFuelSurchargeException): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelSurchargeExceptionCodePrefixRule(var prefix: String) : GlobalFuelSurchargeExceptionRule {
    override fun check(item: GlobalFuelSurchargeException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global fuel surcharge exception. */
data class GlobalFuelSurchargeExceptionValidator(var rules: MutableList<GlobalFuelSurchargeExceptionRule>) {
    fun validate(item: GlobalFuelSurchargeException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalFuelSurchargeException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalFuelSurchargeException>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalFuelSurchargeExceptionValidator(): GlobalFuelSurchargeExceptionValidator {
    return GlobalFuelSurchargeExceptionValidator(
        mutableListOf<GlobalFuelSurchargeExceptionRule>(
            GlobalFuelSurchargeExceptionCodeLengthRule(86400),
            GlobalFuelSurchargeExceptionVehicleIdLengthRule(120),
            GlobalFuelSurchargeExceptionRequiredNameRule("global fuel surcharge exception needs an entry"),
            GlobalFuelSurchargeExceptionRequiredVehicleIdRule(
                "global fuel surcharge exception needs this field",
            ),
            GlobalFuelSurchargeExceptionCodePrefixRule("OPS"),
        ),
    )
}
