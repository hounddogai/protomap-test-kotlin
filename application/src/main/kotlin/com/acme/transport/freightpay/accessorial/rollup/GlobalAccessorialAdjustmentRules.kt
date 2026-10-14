package com.acme.transport.freightpay.accessorial.rollup

/** A rule a global accessorial adjustment must pass. Check returns its problems, or none. */
interface GlobalAccessorialAdjustmentRule {
    fun check(item: GlobalAccessorialAdjustment): MutableList<String>
}

data class GlobalAccessorialAdjustmentRequiredCodeRule(var message: String) : GlobalAccessorialAdjustmentRule {
    override fun check(item: GlobalAccessorialAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalAccessorialAdjustmentDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : GlobalAccessorialAdjustmentRule {
    override fun check(item: GlobalAccessorialAdjustment): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class GlobalAccessorialAdjustmentRequiredLabelRule(var message: String) : GlobalAccessorialAdjustmentRule {
    override fun check(item: GlobalAccessorialAdjustment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalAccessorialAdjustmentCodeLengthRule(var maxLength: Int) : GlobalAccessorialAdjustmentRule {
    override fun check(item: GlobalAccessorialAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAccessorialAdjustmentCodePrefixRule(var prefix: String) : GlobalAccessorialAdjustmentRule {
    override fun check(item: GlobalAccessorialAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global accessorial adjustment. */
data class GlobalAccessorialAdjustmentValidator(var rules: MutableList<GlobalAccessorialAdjustmentRule>) {
    fun validate(item: GlobalAccessorialAdjustment): MutableList<String> {
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

    fun requireValid(item: GlobalAccessorialAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalAccessorialAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalAccessorialAdjustmentValidator(): GlobalAccessorialAdjustmentValidator {
    return GlobalAccessorialAdjustmentValidator(
        mutableListOf<GlobalAccessorialAdjustmentRule>(
            GlobalAccessorialAdjustmentRequiredCodeRule("global accessorial adjustment needs an entry"),
            GlobalAccessorialAdjustmentDistanceKmRangeRule(9.5, 889.8),
            GlobalAccessorialAdjustmentRequiredLabelRule("global accessorial adjustment needs a value"),
            GlobalAccessorialAdjustmentCodeLengthRule(16),
            GlobalAccessorialAdjustmentCodePrefixRule("REF"),
        ),
    )
}
