package com.acme.transport.customs.inspection.inspectionoverride

/** A rule a fallback inspection metric must pass. Check returns its problems, or none. */
interface FallbackInspectionMetricRule {
    fun check(item: FallbackInspectionMetric): MutableList<String>
}

data class FallbackInspectionMetricVehicleIdLengthRule(var maxLength: Int) : FallbackInspectionMetricRule {
    override fun check(item: FallbackInspectionMetric): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackInspectionMetricRequiredNameRule(var message: String) : FallbackInspectionMetricRule {
    override fun check(item: FallbackInspectionMetric): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackInspectionMetricNameLengthRule(var maxLength: Int) : FallbackInspectionMetricRule {
    override fun check(item: FallbackInspectionMetric): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackInspectionMetricDockLengthRule(var maxLength: Int) : FallbackInspectionMetricRule {
    override fun check(item: FallbackInspectionMetric): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackInspectionMetricPalletsRangeRule(var min: Int, var max: Int) : FallbackInspectionMetricRule {
    override fun check(item: FallbackInspectionMetric): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class FallbackInspectionMetricRequiredVehicleIdRule(var message: String) : FallbackInspectionMetricRule {
    override fun check(item: FallbackInspectionMetric): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback inspection metric. */
data class FallbackInspectionMetricValidator(var rules: MutableList<FallbackInspectionMetricRule>) {
    fun validate(item: FallbackInspectionMetric): MutableList<String> {
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

    fun requireValid(item: FallbackInspectionMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackInspectionMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackInspectionMetricValidator(): FallbackInspectionMetricValidator {
    return FallbackInspectionMetricValidator(
        mutableListOf<FallbackInspectionMetricRule>(
            FallbackInspectionMetricVehicleIdLengthRule(255),
            FallbackInspectionMetricRequiredNameRule("fallback inspection metric needs an entry"),
            FallbackInspectionMetricNameLengthRule(32),
            FallbackInspectionMetricDockLengthRule(3600),
            FallbackInspectionMetricPalletsRangeRule(32, 1000),
            FallbackInspectionMetricRequiredVehicleIdRule("fallback inspection metric needs an entry"),
        ),
    )
}
