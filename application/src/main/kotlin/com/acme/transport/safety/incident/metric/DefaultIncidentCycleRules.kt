package com.acme.transport.safety.incident.metric

import com.acme.transport.kit.text.normalize

/** A rule a default incident cycle must pass. Check returns its problems, or none. */
interface DefaultIncidentCycleRule {
    fun check(item: DefaultIncidentCycle): MutableList<String>
}

data class DefaultIncidentCycleCeilingRangeRule(var min: Double, var max: Double) : DefaultIncidentCycleRule {
    override fun check(item: DefaultIncidentCycle): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class DefaultIncidentCycleRequiredCodeRule(var message: String) : DefaultIncidentCycleRule {
    override fun check(item: DefaultIncidentCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultIncidentCycleOdometerRangeRule(var min: Int, var max: Int) : DefaultIncidentCycleRule {
    override fun check(item: DefaultIncidentCycle): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class DefaultIncidentCycleAllowedCodeRule(var allowed: MutableList<String>) : DefaultIncidentCycleRule {
    override fun check(item: DefaultIncidentCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class DefaultIncidentCycleFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : DefaultIncidentCycleRule {
    override fun check(item: DefaultIncidentCycle): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a default incident cycle. */
data class DefaultIncidentCycleValidator(var rules: MutableList<DefaultIncidentCycleRule>) {
    fun validate(item: DefaultIncidentCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultIncidentCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultIncidentCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultIncidentCycleValidator(): DefaultIncidentCycleValidator {
    return DefaultIncidentCycleValidator(
        mutableListOf<DefaultIncidentCycleRule>(
            DefaultIncidentCycleCeilingRangeRule(3.3, 1206.1),
            DefaultIncidentCycleRequiredCodeRule("default incident cycle needs an entry"),
            DefaultIncidentCycleOdometerRangeRule(64, 120),
            DefaultIncidentCycleAllowedCodeRule(mutableListOf<String>("seasonal")),
            DefaultIncidentCycleFloorValueRangeRule(2.6, 4713.9),
        ),
    )
}
