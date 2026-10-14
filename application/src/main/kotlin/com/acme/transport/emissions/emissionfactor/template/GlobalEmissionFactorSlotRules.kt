package com.acme.transport.emissions.emissionfactor.template

/** A rule a global emission factor slot must pass. Check returns its problems, or none. */
interface GlobalEmissionFactorSlotRule {
    fun check(item: GlobalEmissionFactorSlot): MutableList<String>
}

data class GlobalEmissionFactorSlotCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : GlobalEmissionFactorSlotRule {
    override fun check(item: GlobalEmissionFactorSlot): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalEmissionFactorSlotOdometerRangeRule(
    var min: Int,
    var max: Int,
) : GlobalEmissionFactorSlotRule {
    override fun check(item: GlobalEmissionFactorSlot): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalEmissionFactorSlotCodePrefixRule(var prefix: String) : GlobalEmissionFactorSlotRule {
    override fun check(item: GlobalEmissionFactorSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global emission factor slot. */
data class GlobalEmissionFactorSlotValidator(var rules: MutableList<GlobalEmissionFactorSlotRule>) {
    fun validate(item: GlobalEmissionFactorSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalEmissionFactorSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalEmissionFactorSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalEmissionFactorSlotValidator(): GlobalEmissionFactorSlotValidator {
    return GlobalEmissionFactorSlotValidator(
        mutableListOf<GlobalEmissionFactorSlotRule>(
            GlobalEmissionFactorSlotCreatedAtRangeRule(3600, 86400),
            GlobalEmissionFactorSlotOdometerRangeRule(16, 64),
            GlobalEmissionFactorSlotCodePrefixRule("Q"),
        ),
    )
}
