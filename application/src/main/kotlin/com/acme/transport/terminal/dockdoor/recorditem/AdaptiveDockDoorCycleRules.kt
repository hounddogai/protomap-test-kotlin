package com.acme.transport.terminal.dockdoor.recorditem

/** A rule a adaptive dock door cycle must pass. Check returns its problems, or none. */
interface AdaptiveDockDoorCycleRule {
    fun check(item: AdaptiveDockDoorCycle): MutableList<String>
}

data class AdaptiveDockDoorCycleRequiredLabelRule(var message: String) : AdaptiveDockDoorCycleRule {
    override fun check(item: AdaptiveDockDoorCycle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDockDoorCycleCodeLengthRule(var maxLength: Int) : AdaptiveDockDoorCycleRule {
    override fun check(item: AdaptiveDockDoorCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDockDoorCycleRequiredDriverIdRule(var message: String) : AdaptiveDockDoorCycleRule {
    override fun check(item: AdaptiveDockDoorCycle): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDockDoorCycleRatioRangeRule(var min: Double, var max: Double) : AdaptiveDockDoorCycleRule {
    override fun check(item: AdaptiveDockDoorCycle): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class AdaptiveDockDoorCycleMinimumRangeRule(var min: Double, var max: Double) : AdaptiveDockDoorCycleRule {
    override fun check(item: AdaptiveDockDoorCycle): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a adaptive dock door cycle. */
data class AdaptiveDockDoorCycleValidator(var rules: MutableList<AdaptiveDockDoorCycleRule>) {
    fun validate(item: AdaptiveDockDoorCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveDockDoorCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveDockDoorCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveDockDoorCycleValidator(): AdaptiveDockDoorCycleValidator {
    return AdaptiveDockDoorCycleValidator(
        mutableListOf<AdaptiveDockDoorCycleRule>(
            AdaptiveDockDoorCycleRequiredLabelRule("adaptive dock door cycle needs a value"),
            AdaptiveDockDoorCycleCodeLengthRule(255),
            AdaptiveDockDoorCycleRequiredDriverIdRule("adaptive dock door cycle needs a value"),
            AdaptiveDockDoorCycleRatioRangeRule(2.8, 2656.0),
            AdaptiveDockDoorCycleMinimumRangeRule(9.3, 4247.3),
        ),
    )
}
