package com.acme.transport.fleetops.inspection.inspectionschedule

import com.acme.transport.kit.text.normalize

/** A rule a global inspection batch must pass. Check returns its problems, or none. */
interface GlobalInspectionBatchRule {
    fun check(item: GlobalInspectionBatch): MutableList<String>
}

data class GlobalInspectionBatchVehicleIdLengthRule(var maxLength: Int) : GlobalInspectionBatchRule {
    override fun check(item: GlobalInspectionBatch): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionBatchDiscountRangeRule(
    var min: Double,
    var max: Double,
) : GlobalInspectionBatchRule {
    override fun check(item: GlobalInspectionBatch): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class GlobalInspectionBatchAllowedCodeRule(var allowed: MutableList<String>) : GlobalInspectionBatchRule {
    override fun check(item: GlobalInspectionBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionBatchCodeLengthRule(var maxLength: Int) : GlobalInspectionBatchRule {
    override fun check(item: GlobalInspectionBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global inspection batch. */
data class GlobalInspectionBatchValidator(var rules: MutableList<GlobalInspectionBatchRule>) {
    fun validate(item: GlobalInspectionBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalInspectionBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalInspectionBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalInspectionBatchValidator(): GlobalInspectionBatchValidator {
    return GlobalInspectionBatchValidator(
        mutableListOf<GlobalInspectionBatchRule>(
            GlobalInspectionBatchVehicleIdLengthRule(16),
            GlobalInspectionBatchDiscountRangeRule(9.4, 239.2),
            GlobalInspectionBatchAllowedCodeRule(mutableListOf<String>("seasonal", "primary", "adaptive")),
            GlobalInspectionBatchCodeLengthRule(255),
        ),
    )
}
