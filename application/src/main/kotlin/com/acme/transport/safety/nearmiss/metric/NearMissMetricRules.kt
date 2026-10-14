package com.acme.transport.safety.nearmiss.metric

/** A rule a near miss metric must pass. Check returns its problems, or none. */
interface NearMissMetricRule {
    fun check(item: NearMissMetric): MutableList<String>
}

data class NearMissMetricRequiredCodeRule(var message: String) : NearMissMetricRule {
    override fun check(item: NearMissMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissMetricCodeLengthRule(var maxLength: Int) : NearMissMetricRule {
    override fun check(item: NearMissMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissMetricTrailerIdLengthRule(var maxLength: Int) : NearMissMetricRule {
    override fun check(item: NearMissMetric): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss metric. */
data class NearMissMetricValidator(var rules: MutableList<NearMissMetricRule>) {
    fun validate(item: NearMissMetric): MutableList<String> {
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

    fun requireValid(item: NearMissMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissMetricValidator(): NearMissMetricValidator {
    return NearMissMetricValidator(
        mutableListOf<NearMissMetricRule>(
            NearMissMetricRequiredCodeRule("near miss metric needs an entry"),
            NearMissMetricCodeLengthRule(3600),
            NearMissMetricTrailerIdLengthRule(80),
        ),
    )
}
