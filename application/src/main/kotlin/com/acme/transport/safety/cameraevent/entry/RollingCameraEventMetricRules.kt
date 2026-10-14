package com.acme.transport.safety.cameraevent.entry

/** A rule a rolling camera event metric must pass. Check returns its problems, or none. */
interface RollingCameraEventMetricRule {
    fun check(item: RollingCameraEventMetric): MutableList<String>
}

data class RollingCameraEventMetricReviewerLengthRule(var maxLength: Int) : RollingCameraEventMetricRule {
    override fun check(item: RollingCameraEventMetric): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCameraEventMetricRequiredReviewerRule(var message: String) : RollingCameraEventMetricRule {
    override fun check(item: RollingCameraEventMetric): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingCameraEventMetricCodePrefixRule(var prefix: String) : RollingCameraEventMetricRule {
    override fun check(item: RollingCameraEventMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling camera event metric. */
data class RollingCameraEventMetricValidator(var rules: MutableList<RollingCameraEventMetricRule>) {
    fun validate(item: RollingCameraEventMetric): MutableList<String> {
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

    fun requireValid(item: RollingCameraEventMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingCameraEventMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingCameraEventMetricValidator(): RollingCameraEventMetricValidator {
    return RollingCameraEventMetricValidator(
        mutableListOf<RollingCameraEventMetricRule>(
            RollingCameraEventMetricReviewerLengthRule(32),
            RollingCameraEventMetricRequiredReviewerRule("rolling camera event metric needs an entry"),
            RollingCameraEventMetricCodePrefixRule("Q"),
        ),
    )
}
