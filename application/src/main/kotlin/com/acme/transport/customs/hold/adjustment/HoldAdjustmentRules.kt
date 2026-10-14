package com.acme.transport.customs.hold.adjustment

/** A rule a hold adjustment must pass. Check returns its problems, or none. */
interface HoldAdjustmentRule {
    fun check(item: HoldAdjustment): MutableList<String>
}

data class HoldAdjustmentOwnerIdLengthRule(var maxLength: Int) : HoldAdjustmentRule {
    override fun check(item: HoldAdjustment): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldAdjustmentRequiredPhoneRule(var message: String) : HoldAdjustmentRule {
    override fun check(item: HoldAdjustment): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldAdjustmentCodeLengthRule(var maxLength: Int) : HoldAdjustmentRule {
    override fun check(item: HoldAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldAdjustmentRequiredCodeRule(var message: String) : HoldAdjustmentRule {
    override fun check(item: HoldAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldAdjustmentRateRangeRule(var min: Double, var max: Double) : HoldAdjustmentRule {
    override fun check(item: HoldAdjustment): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class HoldAdjustmentApprovedGuardRule(var limit: Double) : HoldAdjustmentRule {
    override fun check(item: HoldAdjustment): MutableList<String> {
        if (item.approved && item.rate > limit) {
            return mutableListOf<String>("rate ${item.rate} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold adjustment. */
data class HoldAdjustmentValidator(var rules: MutableList<HoldAdjustmentRule>) {
    fun validate(item: HoldAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HoldAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldAdjustmentValidator(): HoldAdjustmentValidator {
    return HoldAdjustmentValidator(
        mutableListOf<HoldAdjustmentRule>(
            HoldAdjustmentOwnerIdLengthRule(120),
            HoldAdjustmentRequiredPhoneRule("hold adjustment needs an entry"),
            HoldAdjustmentCodeLengthRule(3600),
            HoldAdjustmentRequiredCodeRule("hold adjustment needs a value"),
            HoldAdjustmentRateRangeRule(6.4, 3057.3),
            HoldAdjustmentApprovedGuardRule(1.5),
        ),
    )
}
