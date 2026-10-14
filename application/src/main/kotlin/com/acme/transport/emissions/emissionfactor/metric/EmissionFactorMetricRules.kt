package com.acme.transport.emissions.emissionfactor.metric

/** A rule a emission factor metric must pass. Check returns its problems, or none. */
interface EmissionFactorMetricRule {
    fun check(item: EmissionFactorMetric): MutableList<String>
}

data class EmissionFactorMetricCodeLengthRule(var maxLength: Int) : EmissionFactorMetricRule {
    override fun check(item: EmissionFactorMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorMetricCodePrefixRule(var prefix: String) : EmissionFactorMetricRule {
    override fun check(item: EmissionFactorMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorMetricRequiredPhoneRule(var message: String) : EmissionFactorMetricRule {
    override fun check(item: EmissionFactorMetric): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorMetricRequiredLabelRule(var message: String) : EmissionFactorMetricRule {
    override fun check(item: EmissionFactorMetric): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor metric. */
data class EmissionFactorMetricValidator(var rules: MutableList<EmissionFactorMetricRule>) {
    fun validate(item: EmissionFactorMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EmissionFactorMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorMetricValidator(): EmissionFactorMetricValidator {
    return EmissionFactorMetricValidator(
        mutableListOf<EmissionFactorMetricRule>(
            EmissionFactorMetricCodeLengthRule(255),
            EmissionFactorMetricCodePrefixRule("Q"),
            EmissionFactorMetricRequiredPhoneRule("emission factor metric needs this field"),
            EmissionFactorMetricRequiredLabelRule("emission factor metric needs this field"),
        ),
    )
}
