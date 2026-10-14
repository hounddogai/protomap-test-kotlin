package com.acme.transport.customs.hold.forecast

/** A rule a primary hold adjustment must pass. Check returns its problems, or none. */
interface PrimaryHoldAdjustmentRule {
    fun check(item: PrimaryHoldAdjustment): MutableList<String>
}

data class PrimaryHoldAdjustmentRequiredCodeRule(var message: String) : PrimaryHoldAdjustmentRule {
    override fun check(item: PrimaryHoldAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryHoldAdjustmentCodeLengthRule(var maxLength: Int) : PrimaryHoldAdjustmentRule {
    override fun check(item: PrimaryHoldAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryHoldAdjustmentRequiredVehicleIdRule(var message: String) : PrimaryHoldAdjustmentRule {
    override fun check(item: PrimaryHoldAdjustment): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryHoldAdjustmentRouteCodeLengthRule(var maxLength: Int) : PrimaryHoldAdjustmentRule {
    override fun check(item: PrimaryHoldAdjustment): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryHoldAdjustmentCodePrefixRule(var prefix: String) : PrimaryHoldAdjustmentRule {
    override fun check(item: PrimaryHoldAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary hold adjustment. */
data class PrimaryHoldAdjustmentValidator(var rules: MutableList<PrimaryHoldAdjustmentRule>) {
    fun validate(item: PrimaryHoldAdjustment): MutableList<String> {
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

    fun requireValid(item: PrimaryHoldAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryHoldAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryHoldAdjustmentValidator(): PrimaryHoldAdjustmentValidator {
    return PrimaryHoldAdjustmentValidator(
        mutableListOf<PrimaryHoldAdjustmentRule>(
            PrimaryHoldAdjustmentRequiredCodeRule("primary hold adjustment needs this field"),
            PrimaryHoldAdjustmentCodeLengthRule(255),
            PrimaryHoldAdjustmentRequiredVehicleIdRule("primary hold adjustment needs a value"),
            PrimaryHoldAdjustmentRouteCodeLengthRule(32),
            PrimaryHoldAdjustmentCodePrefixRule("Q"),
        ),
    )
}
