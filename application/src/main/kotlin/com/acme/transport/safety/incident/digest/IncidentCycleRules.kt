package com.acme.transport.safety.incident.digest

import com.acme.transport.kit.text.normalize

/** A rule a incident cycle must pass. Check returns its problems, or none. */
interface IncidentCycleRule {
    fun check(item: IncidentCycle): MutableList<String>
}

data class IncidentCycleAllowedLabelRule(var allowed: MutableList<String>) : IncidentCycleRule {
    override fun check(item: IncidentCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is expired")
        }
        return mutableListOf<String>()
    }
}

data class IncidentCycleRequiredLabelRule(var message: String) : IncidentCycleRule {
    override fun check(item: IncidentCycle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentCycleWindowDaysRangeRule(var min: Int, var max: Int) : IncidentCycleRule {
    override fun check(item: IncidentCycle): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class IncidentCycleDescriptionLengthRule(var maxLength: Int) : IncidentCycleRule {
    override fun check(item: IncidentCycle): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentCycleRequiredCodeRule(var message: String) : IncidentCycleRule {
    override fun check(item: IncidentCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentCycleLabelLengthRule(var maxLength: Int) : IncidentCycleRule {
    override fun check(item: IncidentCycle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident cycle. */
data class IncidentCycleValidator(var rules: MutableList<IncidentCycleRule>) {
    fun validate(item: IncidentCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IncidentCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentCycleValidator(): IncidentCycleValidator {
    return IncidentCycleValidator(
        mutableListOf<IncidentCycleRule>(
            IncidentCycleAllowedLabelRule(mutableListOf<String>("regional", "weighted")),
            IncidentCycleRequiredLabelRule("incident cycle needs a value"),
            IncidentCycleWindowDaysRangeRule(255, 86400),
            IncidentCycleDescriptionLengthRule(255),
            IncidentCycleRequiredCodeRule("incident cycle needs an entry"),
            IncidentCycleLabelLengthRule(32),
        ),
    )
}
