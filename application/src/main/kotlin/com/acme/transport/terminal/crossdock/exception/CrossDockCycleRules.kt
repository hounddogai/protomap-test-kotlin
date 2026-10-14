package com.acme.transport.terminal.crossdock.exception

/** A rule a cross dock cycle must pass. Check returns its problems, or none. */
interface CrossDockCycleRule {
    fun check(item: CrossDockCycle): MutableList<String>
}

data class CrossDockCycleRequiredCodeRule(var message: String) : CrossDockCycleRule {
    override fun check(item: CrossDockCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockCycleRequiredRouteCodeRule(var message: String) : CrossDockCycleRule {
    override fun check(item: CrossDockCycle): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockCycleCodeLengthRule(var maxLength: Int) : CrossDockCycleRule {
    override fun check(item: CrossDockCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockCycleRequiredReviewerRule(var message: String) : CrossDockCycleRule {
    override fun check(item: CrossDockCycle): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockCycleRateRangeRule(var min: Double, var max: Double) : CrossDockCycleRule {
    override fun check(item: CrossDockCycle): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a cross dock cycle. */
data class CrossDockCycleValidator(var rules: MutableList<CrossDockCycleRule>) {
    fun validate(item: CrossDockCycle): MutableList<String> {
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

    fun requireValid(item: CrossDockCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockCycleValidator(): CrossDockCycleValidator {
    return CrossDockCycleValidator(
        mutableListOf<CrossDockCycleRule>(
            CrossDockCycleRequiredCodeRule("cross dock cycle needs this field"),
            CrossDockCycleRequiredRouteCodeRule("cross dock cycle needs a value"),
            CrossDockCycleCodeLengthRule(64),
            CrossDockCycleRequiredReviewerRule("cross dock cycle needs a value"),
            CrossDockCycleRateRangeRule(7.0, 2353.4),
        ),
    )
}
