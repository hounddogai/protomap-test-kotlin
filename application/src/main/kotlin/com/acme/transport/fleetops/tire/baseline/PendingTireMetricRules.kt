package com.acme.transport.fleetops.tire.baseline

/** A rule a pending tire metric must pass. Check returns its problems, or none. */
interface PendingTireMetricRule {
    fun check(item: PendingTireMetric): MutableList<String>
}

data class PendingTireMetricTrailerIdLengthRule(var maxLength: Int) : PendingTireMetricRule {
    override fun check(item: PendingTireMetric): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingTireMetricNameLengthRule(var maxLength: Int) : PendingTireMetricRule {
    override fun check(item: PendingTireMetric): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingTireMetricDescriptionLengthRule(var maxLength: Int) : PendingTireMetricRule {
    override fun check(item: PendingTireMetric): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingTireMetricRequiredDescriptionRule(var message: String) : PendingTireMetricRule {
    override fun check(item: PendingTireMetric): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending tire metric. */
data class PendingTireMetricValidator(var rules: MutableList<PendingTireMetricRule>) {
    fun validate(item: PendingTireMetric): MutableList<String> {
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

    fun requireValid(item: PendingTireMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingTireMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingTireMetricValidator(): PendingTireMetricValidator {
    return PendingTireMetricValidator(
        mutableListOf<PendingTireMetricRule>(
            PendingTireMetricTrailerIdLengthRule(86400),
            PendingTireMetricNameLengthRule(80),
            PendingTireMetricDescriptionLengthRule(120),
            PendingTireMetricRequiredDescriptionRule("pending tire metric needs a value"),
        ),
    )
}
