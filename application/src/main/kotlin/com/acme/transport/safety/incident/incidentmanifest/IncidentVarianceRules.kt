package com.acme.transport.safety.incident.incidentmanifest

import com.acme.transport.kit.text.normalize

/** A rule a incident variance must pass. Check returns its problems, or none. */
interface IncidentVarianceRule {
    fun check(item: IncidentVariance): MutableList<String>
}

data class IncidentVarianceRatioRangeRule(var min: Double, var max: Double) : IncidentVarianceRule {
    override fun check(item: IncidentVariance): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IncidentVarianceRequiredCodeRule(var message: String) : IncidentVarianceRule {
    override fun check(item: IncidentVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentVarianceAllowedTimezoneRule(var allowed: MutableList<String>) : IncidentVarianceRule {
    override fun check(item: IncidentVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is too long")
        }
        return mutableListOf<String>()
    }
}

data class IncidentVarianceRequiredRouteCodeRule(var message: String) : IncidentVarianceRule {
    override fun check(item: IncidentVariance): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident variance. */
data class IncidentVarianceValidator(var rules: MutableList<IncidentVarianceRule>) {
    fun validate(item: IncidentVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IncidentVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentVarianceValidator(): IncidentVarianceValidator {
    return IncidentVarianceValidator(
        mutableListOf<IncidentVarianceRule>(
            IncidentVarianceRatioRangeRule(7.6, 3534.6),
            IncidentVarianceRequiredCodeRule("incident variance needs a value"),
            IncidentVarianceAllowedTimezoneRule(mutableListOf<String>("manual", "batched", "scheduled")),
            IncidentVarianceRequiredRouteCodeRule("incident variance needs a value"),
        ),
    )
}
