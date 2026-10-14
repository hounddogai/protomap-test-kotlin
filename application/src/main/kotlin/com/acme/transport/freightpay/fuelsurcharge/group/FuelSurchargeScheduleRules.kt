package com.acme.transport.freightpay.fuelsurcharge.group

/** A rule a fuel surcharge schedule must pass. Check returns its problems, or none. */
interface FuelSurchargeScheduleRule {
    fun check(item: FuelSurchargeSchedule): MutableList<String>
}

data class FuelSurchargeScheduleRequiredNameRule(var message: String) : FuelSurchargeScheduleRule {
    override fun check(item: FuelSurchargeSchedule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeScheduleRequiredCodeRule(var message: String) : FuelSurchargeScheduleRule {
    override fun check(item: FuelSurchargeSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeScheduleCodeLengthRule(var maxLength: Int) : FuelSurchargeScheduleRule {
    override fun check(item: FuelSurchargeSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge schedule. */
data class FuelSurchargeScheduleValidator(var rules: MutableList<FuelSurchargeScheduleRule>) {
    fun validate(item: FuelSurchargeSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelSurchargeSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeScheduleValidator(): FuelSurchargeScheduleValidator {
    return FuelSurchargeScheduleValidator(
        mutableListOf<FuelSurchargeScheduleRule>(
            FuelSurchargeScheduleRequiredNameRule("fuel surcharge schedule needs a value"),
            FuelSurchargeScheduleRequiredCodeRule("fuel surcharge schedule needs an entry"),
            FuelSurchargeScheduleCodeLengthRule(1000),
        ),
    )
}
