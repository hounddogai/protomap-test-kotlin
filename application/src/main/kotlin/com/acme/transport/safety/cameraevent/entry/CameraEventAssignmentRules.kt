package com.acme.transport.safety.cameraevent.entry

/** A rule a camera event assignment must pass. Check returns its problems, or none. */
interface CameraEventAssignmentRule {
    fun check(item: CameraEventAssignment): MutableList<String>
}

data class CameraEventAssignmentRateRangeRule(var min: Double, var max: Double) : CameraEventAssignmentRule {
    override fun check(item: CameraEventAssignment): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventAssignmentRequiredCountryRule(var message: String) : CameraEventAssignmentRule {
    override fun check(item: CameraEventAssignment): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventAssignmentMaximumRangeRule(var min: Double, var max: Double) : CameraEventAssignmentRule {
    override fun check(item: CameraEventAssignment): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a camera event assignment. */
data class CameraEventAssignmentValidator(var rules: MutableList<CameraEventAssignmentRule>) {
    fun validate(item: CameraEventAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CameraEventAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventAssignmentValidator(): CameraEventAssignmentValidator {
    return CameraEventAssignmentValidator(
        mutableListOf<CameraEventAssignmentRule>(
            CameraEventAssignmentRateRangeRule(3.3, 639.7),
            CameraEventAssignmentRequiredCountryRule("camera event assignment needs an entry"),
            CameraEventAssignmentMaximumRangeRule(6.2, 1484.6),
        ),
    )
}
