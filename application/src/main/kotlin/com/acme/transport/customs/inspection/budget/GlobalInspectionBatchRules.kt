package com.acme.transport.customs.inspection.budget

/** A rule a global inspection batch must pass. Check returns its problems, or none. */
interface GlobalInspectionBatchRule {
    fun check(item: GlobalInspectionBatch): MutableList<String>
}

data class GlobalInspectionBatchRequiredNameRule(var message: String) : GlobalInspectionBatchRule {
    override fun check(item: GlobalInspectionBatch): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionBatchRequiredDriverIdRule(var message: String) : GlobalInspectionBatchRule {
    override fun check(item: GlobalInspectionBatch): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionBatchCodePrefixRule(var prefix: String) : GlobalInspectionBatchRule {
    override fun check(item: GlobalInspectionBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionBatchPinnedGuardRule(var limit: Int) : GlobalInspectionBatchRule {
    override fun check(item: GlobalInspectionBatch): MutableList<String> {
        if (item.pinned && item.sequence > limit) {
            return mutableListOf<String>("sequence ${item.sequence} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
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

data class GlobalInspectionBatchSequenceRangeRule(var min: Int, var max: Int) : GlobalInspectionBatchRule {
    override fun check(item: GlobalInspectionBatch): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a global inspection batch. */
data class GlobalInspectionBatchValidator(var rules: MutableList<GlobalInspectionBatchRule>) {
    fun validate(item: GlobalInspectionBatch): MutableList<String> {
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

    fun requireValid(item: GlobalInspectionBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
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
            GlobalInspectionBatchRequiredNameRule("global inspection batch needs this field"),
            GlobalInspectionBatchRequiredDriverIdRule("global inspection batch needs a value"),
            GlobalInspectionBatchCodePrefixRule("ACM"),
            GlobalInspectionBatchPinnedGuardRule(1000),
            GlobalInspectionBatchVehicleIdLengthRule(1000),
            GlobalInspectionBatchSequenceRangeRule(64, 64),
        ),
    )
}
