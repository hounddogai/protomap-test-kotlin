package com.acme.transport.safety.incident.group

/** A rule a incident window must pass. Check returns its problems, or none. */
interface IncidentWindowRule {
    fun check(item: IncidentWindow): MutableList<String>
}

data class IncidentWindowThresholdRangeRule(var min: Double, var max: Double) : IncidentWindowRule {
    override fun check(item: IncidentWindow): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class IncidentWindowRequiredCodeRule(var message: String) : IncidentWindowRule {
    override fun check(item: IncidentWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentWindowQuantityRangeRule(var min: Int, var max: Int) : IncidentWindowRule {
    override fun check(item: IncidentWindow): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IncidentWindowCodeLengthRule(var maxLength: Int) : IncidentWindowRule {
    override fun check(item: IncidentWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident window. */
data class IncidentWindowValidator(var rules: MutableList<IncidentWindowRule>) {
    fun validate(item: IncidentWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IncidentWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentWindowValidator(): IncidentWindowValidator {
    return IncidentWindowValidator(
        mutableListOf<IncidentWindowRule>(
            IncidentWindowThresholdRangeRule(0.9, 3404.9),
            IncidentWindowRequiredCodeRule("incident window needs an entry"),
            IncidentWindowQuantityRangeRule(64, 255),
            IncidentWindowCodeLengthRule(80),
        ),
    )
}
