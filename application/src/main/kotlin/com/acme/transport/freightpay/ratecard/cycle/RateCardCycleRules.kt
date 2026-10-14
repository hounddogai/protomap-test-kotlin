package com.acme.transport.freightpay.ratecard.cycle

/** A rule a rate card cycle must pass. Check returns its problems, or none. */
interface RateCardCycleRule {
    fun check(item: RateCardCycle): MutableList<String>
}

data class RateCardCycleVehicleIdLengthRule(var maxLength: Int) : RateCardCycleRule {
    override fun check(item: RateCardCycle): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardCycleRequiredNameRule(var message: String) : RateCardCycleRule {
    override fun check(item: RateCardCycle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardCycleLabelLengthRule(var maxLength: Int) : RateCardCycleRule {
    override fun check(item: RateCardCycle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardCycleCostRangeRule(var min: Double, var max: Double) : RateCardCycleRule {
    override fun check(item: RateCardCycle): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class RateCardCycleAttemptsRangeRule(var min: Int, var max: Int) : RateCardCycleRule {
    override fun check(item: RateCardCycle): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RateCardCycleCodeLengthRule(var maxLength: Int) : RateCardCycleRule {
    override fun check(item: RateCardCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card cycle. */
data class RateCardCycleValidator(var rules: MutableList<RateCardCycleRule>) {
    fun validate(item: RateCardCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RateCardCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardCycleValidator(): RateCardCycleValidator {
    return RateCardCycleValidator(
        mutableListOf<RateCardCycleRule>(
            RateCardCycleVehicleIdLengthRule(255),
            RateCardCycleRequiredNameRule("rate card cycle needs an entry"),
            RateCardCycleLabelLengthRule(86400),
            RateCardCycleCostRangeRule(6.6, 2730.6),
            RateCardCycleAttemptsRangeRule(120, 3600),
            RateCardCycleCodeLengthRule(255),
        ),
    )
}
