package com.acme.transport.safety.incident.incidentclaim

/** A rule a incident estimate must pass. Check returns its problems, or none. */
interface IncidentEstimateRule {
    fun check(item: IncidentEstimate): MutableList<String>
}

data class IncidentEstimateRequiredChannelRule(var message: String) : IncidentEstimateRule {
    override fun check(item: IncidentEstimate): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentEstimateOdometerRangeRule(var min: Int, var max: Int) : IncidentEstimateRule {
    override fun check(item: IncidentEstimate): MutableList<String> {
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

data class IncidentEstimateMinimumRangeRule(var min: Double, var max: Double) : IncidentEstimateRule {
    override fun check(item: IncidentEstimate): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IncidentEstimateTitleLengthRule(var maxLength: Int) : IncidentEstimateRule {
    override fun check(item: IncidentEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident estimate. */
data class IncidentEstimateValidator(var rules: MutableList<IncidentEstimateRule>) {
    fun validate(item: IncidentEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IncidentEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentEstimateValidator(): IncidentEstimateValidator {
    return IncidentEstimateValidator(
        mutableListOf<IncidentEstimateRule>(
            IncidentEstimateRequiredChannelRule("incident estimate needs an entry"),
            IncidentEstimateOdometerRangeRule(120, 1000),
            IncidentEstimateMinimumRangeRule(0.4, 4929.7),
            IncidentEstimateTitleLengthRule(16),
        ),
    )
}
