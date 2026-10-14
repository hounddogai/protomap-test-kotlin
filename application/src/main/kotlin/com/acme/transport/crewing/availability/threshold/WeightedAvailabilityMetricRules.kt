package com.acme.transport.crewing.availability.threshold

/** A rule a weighted availability metric must pass. Check returns its problems, or none. */
interface WeightedAvailabilityMetricRule {
    fun check(item: WeightedAvailabilityMetric): MutableList<String>
}

data class WeightedAvailabilityMetricRequiredVehicleIdRule(
    var message: String,
) : WeightedAvailabilityMetricRule {
    override fun check(item: WeightedAvailabilityMetric): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedAvailabilityMetricChecksumLengthRule(var maxLength: Int) : WeightedAvailabilityMetricRule {
    override fun check(item: WeightedAvailabilityMetric): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAvailabilityMetricCodePrefixRule(var prefix: String) : WeightedAvailabilityMetricRule {
    override fun check(item: WeightedAvailabilityMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAvailabilityMetricLabelLengthRule(var maxLength: Int) : WeightedAvailabilityMetricRule {
    override fun check(item: WeightedAvailabilityMetric): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAvailabilityMetricVehicleIdLengthRule(var maxLength: Int) : WeightedAvailabilityMetricRule {
    override fun check(item: WeightedAvailabilityMetric): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted availability metric. */
data class WeightedAvailabilityMetricValidator(var rules: MutableList<WeightedAvailabilityMetricRule>) {
    fun validate(item: WeightedAvailabilityMetric): MutableList<String> {
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

    fun requireValid(item: WeightedAvailabilityMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedAvailabilityMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedAvailabilityMetricValidator(): WeightedAvailabilityMetricValidator {
    return WeightedAvailabilityMetricValidator(
        mutableListOf<WeightedAvailabilityMetricRule>(
            WeightedAvailabilityMetricRequiredVehicleIdRule("weighted availability metric needs an entry"),
            WeightedAvailabilityMetricChecksumLengthRule(255),
            WeightedAvailabilityMetricCodePrefixRule("X"),
            WeightedAvailabilityMetricLabelLengthRule(16),
            WeightedAvailabilityMetricVehicleIdLengthRule(1000),
        ),
    )
}
