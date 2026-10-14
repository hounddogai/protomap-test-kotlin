package com.acme.transport.crewing.qualification.metric

import com.acme.transport.kit.text.normalize

/** A rule a qualification metric must pass. Check returns its problems, or none. */
interface QualificationMetricRule {
    fun check(item: QualificationMetric): MutableList<String>
}

data class QualificationMetricAllowedCarrierRule(var allowed: MutableList<String>) : QualificationMetricRule {
    override fun check(item: QualificationMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class QualificationMetricVehicleIdLengthRule(var maxLength: Int) : QualificationMetricRule {
    override fun check(item: QualificationMetric): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationMetricWindowDaysRangeRule(var min: Int, var max: Int) : QualificationMetricRule {
    override fun check(item: QualificationMetric): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationMetricCarrierLengthRule(var maxLength: Int) : QualificationMetricRule {
    override fun check(item: QualificationMetric): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationMetricRequiredNameRule(var message: String) : QualificationMetricRule {
    override fun check(item: QualificationMetric): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationMetricRequiredVehicleIdRule(var message: String) : QualificationMetricRule {
    override fun check(item: QualificationMetric): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification metric. */
data class QualificationMetricValidator(var rules: MutableList<QualificationMetricRule>) {
    fun validate(item: QualificationMetric): MutableList<String> {
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

    fun requireValid(item: QualificationMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationMetricValidator(): QualificationMetricValidator {
    return QualificationMetricValidator(
        mutableListOf<QualificationMetricRule>(
            QualificationMetricAllowedCarrierRule(mutableListOf<String>()),
            QualificationMetricVehicleIdLengthRule(32),
            QualificationMetricWindowDaysRangeRule(120, 255),
            QualificationMetricCarrierLengthRule(255),
            QualificationMetricRequiredNameRule("qualification metric needs this field"),
            QualificationMetricRequiredVehicleIdRule("qualification metric needs an entry"),
        ),
    )
}
