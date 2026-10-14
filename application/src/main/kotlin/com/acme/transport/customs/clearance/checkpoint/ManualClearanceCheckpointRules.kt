package com.acme.transport.customs.clearance.checkpoint

/** A rule a manual clearance checkpoint must pass. Check returns its problems, or none. */
interface ManualClearanceCheckpointRule {
    fun check(item: ManualClearanceCheckpoint): MutableList<String>
}

data class ManualClearanceCheckpointRouteCodeLengthRule(var maxLength: Int) : ManualClearanceCheckpointRule {
    override fun check(item: ManualClearanceCheckpoint): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualClearanceCheckpointRequiredRouteCodeRule(var message: String) : ManualClearanceCheckpointRule {
    override fun check(item: ManualClearanceCheckpoint): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualClearanceCheckpointCodePrefixRule(var prefix: String) : ManualClearanceCheckpointRule {
    override fun check(item: ManualClearanceCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualClearanceCheckpointWeightRangeRule(
    var min: Double,
    var max: Double,
) : ManualClearanceCheckpointRule {
    override fun check(item: ManualClearanceCheckpoint): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualClearanceCheckpointGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : ManualClearanceCheckpointRule {
    override fun check(item: ManualClearanceCheckpoint): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class ManualClearanceCheckpointDiscountRangeRule(
    var min: Double,
    var max: Double,
) : ManualClearanceCheckpointRule {
    override fun check(item: ManualClearanceCheckpoint): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual clearance checkpoint. */
data class ManualClearanceCheckpointValidator(var rules: MutableList<ManualClearanceCheckpointRule>) {
    fun validate(item: ManualClearanceCheckpoint): MutableList<String> {
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

    fun requireValid(item: ManualClearanceCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualClearanceCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultManualClearanceCheckpointValidator(): ManualClearanceCheckpointValidator {
    return ManualClearanceCheckpointValidator(
        mutableListOf<ManualClearanceCheckpointRule>(
            ManualClearanceCheckpointRouteCodeLengthRule(32),
            ManualClearanceCheckpointRequiredRouteCodeRule("manual clearance checkpoint needs an entry"),
            ManualClearanceCheckpointCodePrefixRule("REF"),
            ManualClearanceCheckpointWeightRangeRule(9.8, 4149.6),
            ManualClearanceCheckpointGraceDaysRangeRule(32, 3600),
            ManualClearanceCheckpointDiscountRangeRule(0.6, 3360.5),
        ),
    )
}
