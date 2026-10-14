package com.acme.transport.freightpay.fuelsurcharge.audit

/** A rule a regional fuel surcharge audit must pass. Check returns its problems, or none. */
interface RegionalFuelSurchargeAuditRule {
    fun check(item: RegionalFuelSurchargeAudit): MutableList<String>
}

data class RegionalFuelSurchargeAuditCodeLengthRule(var maxLength: Int) : RegionalFuelSurchargeAuditRule {
    override fun check(item: RegionalFuelSurchargeAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelSurchargeAuditRequiredNameRule(var message: String) : RegionalFuelSurchargeAuditRule {
    override fun check(item: RegionalFuelSurchargeAudit): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelSurchargeAuditDiscountRangeRule(
    var min: Double,
    var max: Double,
) : RegionalFuelSurchargeAuditRule {
    override fun check(item: RegionalFuelSurchargeAudit): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelSurchargeAuditAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : RegionalFuelSurchargeAuditRule {
    override fun check(item: RegionalFuelSurchargeAudit): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelSurchargeAuditRequiredCurrencyRule(
    var message: String,
) : RegionalFuelSurchargeAuditRule {
    override fun check(item: RegionalFuelSurchargeAudit): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelSurchargeAuditCodePrefixRule(var prefix: String) : RegionalFuelSurchargeAuditRule {
    override fun check(item: RegionalFuelSurchargeAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional fuel surcharge audit. */
data class RegionalFuelSurchargeAuditValidator(var rules: MutableList<RegionalFuelSurchargeAuditRule>) {
    fun validate(item: RegionalFuelSurchargeAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalFuelSurchargeAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalFuelSurchargeAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalFuelSurchargeAuditValidator(): RegionalFuelSurchargeAuditValidator {
    return RegionalFuelSurchargeAuditValidator(
        mutableListOf<RegionalFuelSurchargeAuditRule>(
            RegionalFuelSurchargeAuditCodeLengthRule(86400),
            RegionalFuelSurchargeAuditRequiredNameRule("regional fuel surcharge audit needs an entry"),
            RegionalFuelSurchargeAuditDiscountRangeRule(8.9, 1311.9),
            RegionalFuelSurchargeAuditAttemptsRangeRule(1000, 1000),
            RegionalFuelSurchargeAuditRequiredCurrencyRule("regional fuel surcharge audit needs this field"),
            RegionalFuelSurchargeAuditCodePrefixRule("ACM"),
        ),
    )
}
