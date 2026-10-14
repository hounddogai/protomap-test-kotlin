package com.acme.transport.terminal.dockdoor.metric

/** A rule a dock door metric must pass. Check returns its problems, or none. */
interface DockDoorMetricRule {
    fun check(item: DockDoorMetric): MutableList<String>
}

data class DockDoorMetricFuelLitersRangeRule(var min: Double, var max: Double) : DockDoorMetricRule {
    override fun check(item: DockDoorMetric): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorMetricVehicleIdLengthRule(var maxLength: Int) : DockDoorMetricRule {
    override fun check(item: DockDoorMetric): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorMetricOwnerIdLengthRule(var maxLength: Int) : DockDoorMetricRule {
    override fun check(item: DockDoorMetric): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorMetricRequiredNameRule(var message: String) : DockDoorMetricRule {
    override fun check(item: DockDoorMetric): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorMetricNameLengthRule(var maxLength: Int) : DockDoorMetricRule {
    override fun check(item: DockDoorMetric): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorMetricCodeLengthRule(var maxLength: Int) : DockDoorMetricRule {
    override fun check(item: DockDoorMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door metric. */
data class DockDoorMetricValidator(var rules: MutableList<DockDoorMetricRule>) {
    fun validate(item: DockDoorMetric): MutableList<String> {
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

    fun requireValid(item: DockDoorMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorMetricValidator(): DockDoorMetricValidator {
    return DockDoorMetricValidator(
        mutableListOf<DockDoorMetricRule>(
            DockDoorMetricFuelLitersRangeRule(9.0, 4940.9),
            DockDoorMetricVehicleIdLengthRule(1000),
            DockDoorMetricOwnerIdLengthRule(80),
            DockDoorMetricRequiredNameRule("dock door metric needs this field"),
            DockDoorMetricNameLengthRule(32),
            DockDoorMetricCodeLengthRule(255),
        ),
    )
}
