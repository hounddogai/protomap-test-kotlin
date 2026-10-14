package com.acme.transport.freightpay.fuelsurcharge.snapshot

/** A rule a fuel surcharge audit must pass. Check returns its problems, or none. */
interface FuelSurchargeAuditRule {
    fun check(item: FuelSurchargeAudit): MutableList<String>
}

data class FuelSurchargeAuditCodeLengthRule(var maxLength: Int) : FuelSurchargeAuditRule {
    override fun check(item: FuelSurchargeAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeAuditCarrierLengthRule(var maxLength: Int) : FuelSurchargeAuditRule {
    override fun check(item: FuelSurchargeAudit): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeAuditUnitLengthRule(var maxLength: Int) : FuelSurchargeAuditRule {
    override fun check(item: FuelSurchargeAudit): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeAuditRequiredCarrierRule(var message: String) : FuelSurchargeAuditRule {
    override fun check(item: FuelSurchargeAudit): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeAuditTitleLengthRule(var maxLength: Int) : FuelSurchargeAuditRule {
    override fun check(item: FuelSurchargeAudit): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge audit. */
data class FuelSurchargeAuditValidator(var rules: MutableList<FuelSurchargeAuditRule>) {
    fun validate(item: FuelSurchargeAudit): MutableList<String> {
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

    fun requireValid(item: FuelSurchargeAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeAuditValidator(): FuelSurchargeAuditValidator {
    return FuelSurchargeAuditValidator(
        mutableListOf<FuelSurchargeAuditRule>(
            FuelSurchargeAuditCodeLengthRule(32),
            FuelSurchargeAuditCarrierLengthRule(120),
            FuelSurchargeAuditUnitLengthRule(120),
            FuelSurchargeAuditRequiredCarrierRule("fuel surcharge audit needs a value"),
            FuelSurchargeAuditTitleLengthRule(86400),
        ),
    )
}
