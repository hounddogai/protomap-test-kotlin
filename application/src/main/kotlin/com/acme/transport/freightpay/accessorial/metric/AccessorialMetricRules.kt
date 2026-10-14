package com.acme.transport.freightpay.accessorial.metric

/** A rule a accessorial metric must pass. Check returns its problems, or none. */
interface AccessorialMetricRule {
    fun check(item: AccessorialMetric): MutableList<String>
}

data class AccessorialMetricRequiredCodeRule(var message: String) : AccessorialMetricRule {
    override fun check(item: AccessorialMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialMetricRequiredPhoneRule(var message: String) : AccessorialMetricRule {
    override fun check(item: AccessorialMetric): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialMetricMinimumRangeRule(var min: Double, var max: Double) : AccessorialMetricRule {
    override fun check(item: AccessorialMetric): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialMetricCodePrefixRule(var prefix: String) : AccessorialMetricRule {
    override fun check(item: AccessorialMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial metric. */
data class AccessorialMetricValidator(var rules: MutableList<AccessorialMetricRule>) {
    fun validate(item: AccessorialMetric): MutableList<String> {
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

    fun requireValid(item: AccessorialMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialMetricValidator(): AccessorialMetricValidator {
    return AccessorialMetricValidator(
        mutableListOf<AccessorialMetricRule>(
            AccessorialMetricRequiredCodeRule("accessorial metric needs this field"),
            AccessorialMetricRequiredPhoneRule("accessorial metric needs this field"),
            AccessorialMetricMinimumRangeRule(3.2, 539.3),
            AccessorialMetricCodePrefixRule("OPS"),
        ),
    )
}
