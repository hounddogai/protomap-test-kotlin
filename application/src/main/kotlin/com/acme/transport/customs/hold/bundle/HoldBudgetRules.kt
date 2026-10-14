package com.acme.transport.customs.hold.bundle

/** A rule a hold budget must pass. Check returns its problems, or none. */
interface HoldBudgetRule {
    fun check(item: HoldBudget): MutableList<String>
}

data class HoldBudgetSourceLengthRule(var maxLength: Int) : HoldBudgetRule {
    override fun check(item: HoldBudget): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldBudgetNameLengthRule(var maxLength: Int) : HoldBudgetRule {
    override fun check(item: HoldBudget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldBudgetCodeLengthRule(var maxLength: Int) : HoldBudgetRule {
    override fun check(item: HoldBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldBudgetVehicleIdLengthRule(var maxLength: Int) : HoldBudgetRule {
    override fun check(item: HoldBudget): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldBudgetRequiredVehicleIdRule(var message: String) : HoldBudgetRule {
    override fun check(item: HoldBudget): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldBudgetCodePrefixRule(var prefix: String) : HoldBudgetRule {
    override fun check(item: HoldBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold budget. */
data class HoldBudgetValidator(var rules: MutableList<HoldBudgetRule>) {
    fun validate(item: HoldBudget): MutableList<String> {
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

    fun requireValid(item: HoldBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldBudgetValidator(): HoldBudgetValidator {
    return HoldBudgetValidator(
        mutableListOf<HoldBudgetRule>(
            HoldBudgetSourceLengthRule(120),
            HoldBudgetNameLengthRule(3600),
            HoldBudgetCodeLengthRule(86400),
            HoldBudgetVehicleIdLengthRule(1000),
            HoldBudgetRequiredVehicleIdRule("hold budget needs this field"),
            HoldBudgetCodePrefixRule("REF"),
        ),
    )
}
