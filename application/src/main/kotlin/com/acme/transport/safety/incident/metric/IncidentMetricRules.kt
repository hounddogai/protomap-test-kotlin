package com.acme.transport.safety.incident.metric

/** A rule a incident metric must pass. Check returns its problems, or none. */
interface IncidentMetricRule {
    fun check(item: IncidentMetric): MutableList<String>
}

data class IncidentMetricCodeLengthRule(var maxLength: Int) : IncidentMetricRule {
    override fun check(item: IncidentMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentMetricSequenceRangeRule(var min: Int, var max: Int) : IncidentMetricRule {
    override fun check(item: IncidentMetric): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IncidentMetricNotesLengthRule(var maxLength: Int) : IncidentMetricRule {
    override fun check(item: IncidentMetric): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentMetricCodePrefixRule(var prefix: String) : IncidentMetricRule {
    override fun check(item: IncidentMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class IncidentMetricRequiredCodeRule(var message: String) : IncidentMetricRule {
    override fun check(item: IncidentMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident metric. */
data class IncidentMetricValidator(var rules: MutableList<IncidentMetricRule>) {
    fun validate(item: IncidentMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IncidentMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentMetricValidator(): IncidentMetricValidator {
    return IncidentMetricValidator(
        mutableListOf<IncidentMetricRule>(
            IncidentMetricCodeLengthRule(1000),
            IncidentMetricSequenceRangeRule(16, 3600),
            IncidentMetricNotesLengthRule(1000),
            IncidentMetricCodePrefixRule("ACM"),
            IncidentMetricRequiredCodeRule("incident metric needs an entry"),
        ),
    )
}
