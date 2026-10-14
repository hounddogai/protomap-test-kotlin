package com.acme.transport.network.transittime.cycle

/** A rule a global transit time cycle must pass. Check returns its problems, or none. */
interface GlobalTransitTimeCycleRule {
    fun check(item: GlobalTransitTimeCycle): MutableList<String>
}

data class GlobalTransitTimeCycleRequiredContactEmailRule(var message: String) : GlobalTransitTimeCycleRule {
    override fun check(item: GlobalTransitTimeCycle): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalTransitTimeCycleCarrierLengthRule(var maxLength: Int) : GlobalTransitTimeCycleRule {
    override fun check(item: GlobalTransitTimeCycle): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTransitTimeCycleRateRangeRule(var min: Double, var max: Double) : GlobalTransitTimeCycleRule {
    override fun check(item: GlobalTransitTimeCycle): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTransitTimeCycleRequiredLabelRule(var message: String) : GlobalTransitTimeCycleRule {
    override fun check(item: GlobalTransitTimeCycle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global transit time cycle. */
data class GlobalTransitTimeCycleValidator(var rules: MutableList<GlobalTransitTimeCycleRule>) {
    fun validate(item: GlobalTransitTimeCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalTransitTimeCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTransitTimeCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTransitTimeCycleValidator(): GlobalTransitTimeCycleValidator {
    return GlobalTransitTimeCycleValidator(
        mutableListOf<GlobalTransitTimeCycleRule>(
            GlobalTransitTimeCycleRequiredContactEmailRule("global transit time cycle needs a value"),
            GlobalTransitTimeCycleCarrierLengthRule(64),
            GlobalTransitTimeCycleRateRangeRule(6.2, 2694.1),
            GlobalTransitTimeCycleRequiredLabelRule("global transit time cycle needs an entry"),
        ),
    )
}
