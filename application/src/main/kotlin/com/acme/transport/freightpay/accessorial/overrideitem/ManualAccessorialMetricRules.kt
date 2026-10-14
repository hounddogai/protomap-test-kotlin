package com.acme.transport.freightpay.accessorial.overrideitem

/** A rule a manual accessorial metric must pass. Check returns its problems, or none. */
interface ManualAccessorialMetricRule {
    fun check(item: ManualAccessorialMetric): MutableList<String>
}

data class ManualAccessorialMetricRequiredLabelRule(var message: String) : ManualAccessorialMetricRule {
    override fun check(item: ManualAccessorialMetric): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualAccessorialMetricCodeLengthRule(var maxLength: Int) : ManualAccessorialMetricRule {
    override fun check(item: ManualAccessorialMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualAccessorialMetricLocaleLengthRule(var maxLength: Int) : ManualAccessorialMetricRule {
    override fun check(item: ManualAccessorialMetric): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualAccessorialMetricRequiredCodeRule(var message: String) : ManualAccessorialMetricRule {
    override fun check(item: ManualAccessorialMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual accessorial metric. */
data class ManualAccessorialMetricValidator(var rules: MutableList<ManualAccessorialMetricRule>) {
    fun validate(item: ManualAccessorialMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualAccessorialMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualAccessorialMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultManualAccessorialMetricValidator(): ManualAccessorialMetricValidator {
    return ManualAccessorialMetricValidator(
        mutableListOf<ManualAccessorialMetricRule>(
            ManualAccessorialMetricRequiredLabelRule("manual accessorial metric needs an entry"),
            ManualAccessorialMetricCodeLengthRule(32),
            ManualAccessorialMetricLocaleLengthRule(16),
            ManualAccessorialMetricRequiredCodeRule("manual accessorial metric needs an entry"),
        ),
    )
}
