package com.acme.transport.customs.clearance.cycle

/** A rule a rolling clearance adjustment must pass. Check returns its problems, or none. */
interface RollingClearanceAdjustmentRule {
    fun check(item: RollingClearanceAdjustment): MutableList<String>
}

data class RollingClearanceAdjustmentCodeLengthRule(var maxLength: Int) : RollingClearanceAdjustmentRule {
    override fun check(item: RollingClearanceAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingClearanceAdjustmentPinnedGuardRule(var limit: Double) : RollingClearanceAdjustmentRule {
    override fun check(item: RollingClearanceAdjustment): MutableList<String> {
        if (item.pinned && item.minimum > limit) {
            return mutableListOf<String>("minimum ${item.minimum} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class RollingClearanceAdjustmentCodePrefixRule(var prefix: String) : RollingClearanceAdjustmentRule {
    override fun check(item: RollingClearanceAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingClearanceAdjustmentLaneLengthRule(var maxLength: Int) : RollingClearanceAdjustmentRule {
    override fun check(item: RollingClearanceAdjustment): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling clearance adjustment. */
data class RollingClearanceAdjustmentValidator(var rules: MutableList<RollingClearanceAdjustmentRule>) {
    fun validate(item: RollingClearanceAdjustment): MutableList<String> {
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

    fun requireValid(item: RollingClearanceAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingClearanceAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingClearanceAdjustmentValidator(): RollingClearanceAdjustmentValidator {
    return RollingClearanceAdjustmentValidator(
        mutableListOf<RollingClearanceAdjustmentRule>(
            RollingClearanceAdjustmentCodeLengthRule(64),
            RollingClearanceAdjustmentPinnedGuardRule(9.9),
            RollingClearanceAdjustmentCodePrefixRule("REF"),
            RollingClearanceAdjustmentLaneLengthRule(3600),
        ),
    )
}
