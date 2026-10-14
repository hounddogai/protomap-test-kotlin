package com.acme.transport.terminal.traileryard.rollup

/** A rule a manual trailer yard checkpoint must pass. Check returns its problems, or none. */
interface ManualTrailerYardCheckpointRule {
    fun check(item: ManualTrailerYardCheckpoint): MutableList<String>
}

data class ManualTrailerYardCheckpointRequiredLabelRule(var message: String) : ManualTrailerYardCheckpointRule {
    override fun check(item: ManualTrailerYardCheckpoint): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerYardCheckpointRequiredRouteCodeRule(
    var message: String,
) : ManualTrailerYardCheckpointRule {
    override fun check(item: ManualTrailerYardCheckpoint): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerYardCheckpointCodePrefixRule(var prefix: String) : ManualTrailerYardCheckpointRule {
    override fun check(item: ManualTrailerYardCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerYardCheckpointVehicleIdLengthRule(
    var maxLength: Int,
) : ManualTrailerYardCheckpointRule {
    override fun check(item: ManualTrailerYardCheckpoint): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerYardCheckpointPalletsRangeRule(
    var min: Int,
    var max: Int,
) : ManualTrailerYardCheckpointRule {
    override fun check(item: ManualTrailerYardCheckpoint): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerYardCheckpointRequiredCodeRule(var message: String) : ManualTrailerYardCheckpointRule {
    override fun check(item: ManualTrailerYardCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual trailer yard checkpoint. */
data class ManualTrailerYardCheckpointValidator(var rules: MutableList<ManualTrailerYardCheckpointRule>) {
    fun validate(item: ManualTrailerYardCheckpoint): MutableList<String> {
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

    fun requireValid(item: ManualTrailerYardCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTrailerYardCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTrailerYardCheckpointValidator(): ManualTrailerYardCheckpointValidator {
    return ManualTrailerYardCheckpointValidator(
        mutableListOf<ManualTrailerYardCheckpointRule>(
            ManualTrailerYardCheckpointRequiredLabelRule("manual trailer yard checkpoint needs this field"),
            ManualTrailerYardCheckpointRequiredRouteCodeRule("manual trailer yard checkpoint needs a value"),
            ManualTrailerYardCheckpointCodePrefixRule("REF"),
            ManualTrailerYardCheckpointVehicleIdLengthRule(255),
            ManualTrailerYardCheckpointPalletsRangeRule(64, 1000),
            ManualTrailerYardCheckpointRequiredCodeRule("manual trailer yard checkpoint needs a value"),
        ),
    )
}
