package com.acme.transport.fleetops.serviceplan.score

/** A rule a service plan slot must pass. Check returns its problems, or none. */
interface ServicePlanSlotRule {
    fun check(item: ServicePlanSlot): MutableList<String>
}

data class ServicePlanSlotRequiredCodeRule(var message: String) : ServicePlanSlotRule {
    override fun check(item: ServicePlanSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanSlotRequiredCountryRule(var message: String) : ServicePlanSlotRule {
    override fun check(item: ServicePlanSlot): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanSlotRequiredShiftCodeRule(var message: String) : ServicePlanSlotRule {
    override fun check(item: ServicePlanSlot): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanSlotAmountRangeRule(var min: Double, var max: Double) : ServicePlanSlotRule {
    override fun check(item: ServicePlanSlot): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanSlotShiftCodeLengthRule(var maxLength: Int) : ServicePlanSlotRule {
    override fun check(item: ServicePlanSlot): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan slot. */
data class ServicePlanSlotValidator(var rules: MutableList<ServicePlanSlotRule>) {
    fun validate(item: ServicePlanSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServicePlanSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanSlotValidator(): ServicePlanSlotValidator {
    return ServicePlanSlotValidator(
        mutableListOf<ServicePlanSlotRule>(
            ServicePlanSlotRequiredCodeRule("service plan slot needs a value"),
            ServicePlanSlotRequiredCountryRule("service plan slot needs this field"),
            ServicePlanSlotRequiredShiftCodeRule("service plan slot needs a value"),
            ServicePlanSlotAmountRangeRule(8.0, 1020.7),
            ServicePlanSlotShiftCodeLengthRule(86400),
        ),
    )
}
