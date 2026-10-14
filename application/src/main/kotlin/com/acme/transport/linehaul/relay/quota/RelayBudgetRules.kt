package com.acme.transport.linehaul.relay.quota

/** A rule a relay budget must pass. Check returns its problems, or none. */
interface RelayBudgetRule {
    fun check(item: RelayBudget): MutableList<String>
}

data class RelayBudgetRequiredRegionRule(var message: String) : RelayBudgetRule {
    override fun check(item: RelayBudget): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayBudgetVehicleIdLengthRule(var maxLength: Int) : RelayBudgetRule {
    override fun check(item: RelayBudget): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayBudgetCodeLengthRule(var maxLength: Int) : RelayBudgetRule {
    override fun check(item: RelayBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayBudgetRequiredVehicleIdRule(var message: String) : RelayBudgetRule {
    override fun check(item: RelayBudget): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayBudgetRegionLengthRule(var maxLength: Int) : RelayBudgetRule {
    override fun check(item: RelayBudget): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayBudgetCodePrefixRule(var prefix: String) : RelayBudgetRule {
    override fun check(item: RelayBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay budget. */
data class RelayBudgetValidator(var rules: MutableList<RelayBudgetRule>) {
    fun validate(item: RelayBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RelayBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayBudgetValidator(): RelayBudgetValidator {
    return RelayBudgetValidator(
        mutableListOf<RelayBudgetRule>(
            RelayBudgetRequiredRegionRule("relay budget needs this field"),
            RelayBudgetVehicleIdLengthRule(120),
            RelayBudgetCodeLengthRule(255),
            RelayBudgetRequiredVehicleIdRule("relay budget needs this field"),
            RelayBudgetRegionLengthRule(1000),
            RelayBudgetCodePrefixRule("Q"),
        ),
    )
}
