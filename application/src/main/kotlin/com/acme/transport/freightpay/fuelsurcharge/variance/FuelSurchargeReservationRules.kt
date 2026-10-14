package com.acme.transport.freightpay.fuelsurcharge.variance

/** A rule a fuel surcharge reservation must pass. Check returns its problems, or none. */
interface FuelSurchargeReservationRule {
    fun check(item: FuelSurchargeReservation): MutableList<String>
}

data class FuelSurchargeReservationCurrencyLengthRule(var maxLength: Int) : FuelSurchargeReservationRule {
    override fun check(item: FuelSurchargeReservation): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeReservationDockLengthRule(var maxLength: Int) : FuelSurchargeReservationRule {
    override fun check(item: FuelSurchargeReservation): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeReservationRequiredDockRule(var message: String) : FuelSurchargeReservationRule {
    override fun check(item: FuelSurchargeReservation): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge reservation. */
data class FuelSurchargeReservationValidator(var rules: MutableList<FuelSurchargeReservationRule>) {
    fun validate(item: FuelSurchargeReservation): MutableList<String> {
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

    fun requireValid(item: FuelSurchargeReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeReservationValidator(): FuelSurchargeReservationValidator {
    return FuelSurchargeReservationValidator(
        mutableListOf<FuelSurchargeReservationRule>(
            FuelSurchargeReservationCurrencyLengthRule(1000),
            FuelSurchargeReservationDockLengthRule(3600),
            FuelSurchargeReservationRequiredDockRule("fuel surcharge reservation needs a value"),
        ),
    )
}
