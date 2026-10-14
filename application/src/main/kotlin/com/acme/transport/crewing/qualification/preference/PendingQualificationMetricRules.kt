package com.acme.transport.crewing.qualification.preference

/** A rule a pending qualification metric must pass. Check returns its problems, or none. */
interface PendingQualificationMetricRule {
    fun check(item: PendingQualificationMetric): MutableList<String>
}

data class PendingQualificationMetricRequiredCodeRule(var message: String) : PendingQualificationMetricRule {
    override fun check(item: PendingQualificationMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingQualificationMetricTitleLengthRule(var maxLength: Int) : PendingQualificationMetricRule {
    override fun check(item: PendingQualificationMetric): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingQualificationMetricLabelLengthRule(var maxLength: Int) : PendingQualificationMetricRule {
    override fun check(item: PendingQualificationMetric): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingQualificationMetricCodeLengthRule(var maxLength: Int) : PendingQualificationMetricRule {
    override fun check(item: PendingQualificationMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending qualification metric. */
data class PendingQualificationMetricValidator(var rules: MutableList<PendingQualificationMetricRule>) {
    fun validate(item: PendingQualificationMetric): MutableList<String> {
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

    fun requireValid(item: PendingQualificationMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingQualificationMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingQualificationMetricValidator(): PendingQualificationMetricValidator {
    return PendingQualificationMetricValidator(
        mutableListOf<PendingQualificationMetricRule>(
            PendingQualificationMetricRequiredCodeRule("pending qualification metric needs an entry"),
            PendingQualificationMetricTitleLengthRule(32),
            PendingQualificationMetricLabelLengthRule(120),
            PendingQualificationMetricCodeLengthRule(3600),
        ),
    )
}
