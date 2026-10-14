package com.acme.transport.customs.broker.allocation

/** A rule a global broker adjustment must pass. Check returns its problems, or none. */
interface GlobalBrokerAdjustmentRule {
    fun check(item: GlobalBrokerAdjustment): MutableList<String>
}

data class GlobalBrokerAdjustmentCodeLengthRule(var maxLength: Int) : GlobalBrokerAdjustmentRule {
    override fun check(item: GlobalBrokerAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalBrokerAdjustmentRequiredNameRule(var message: String) : GlobalBrokerAdjustmentRule {
    override fun check(item: GlobalBrokerAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalBrokerAdjustmentDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : GlobalBrokerAdjustmentRule {
    override fun check(item: GlobalBrokerAdjustment): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalBrokerAdjustmentLabelLengthRule(var maxLength: Int) : GlobalBrokerAdjustmentRule {
    override fun check(item: GlobalBrokerAdjustment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalBrokerAdjustmentActiveGuardRule(var limit: Int) : GlobalBrokerAdjustmentRule {
    override fun check(item: GlobalBrokerAdjustment): MutableList<String> {
        if (item.active && item.expiresAt > limit) {
            return mutableListOf<String>("expires at ${item.expiresAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class GlobalBrokerAdjustmentNameLengthRule(var maxLength: Int) : GlobalBrokerAdjustmentRule {
    override fun check(item: GlobalBrokerAdjustment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global broker adjustment. */
data class GlobalBrokerAdjustmentValidator(var rules: MutableList<GlobalBrokerAdjustmentRule>) {
    fun validate(item: GlobalBrokerAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalBrokerAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalBrokerAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalBrokerAdjustmentValidator(): GlobalBrokerAdjustmentValidator {
    return GlobalBrokerAdjustmentValidator(
        mutableListOf<GlobalBrokerAdjustmentRule>(
            GlobalBrokerAdjustmentCodeLengthRule(120),
            GlobalBrokerAdjustmentRequiredNameRule("global broker adjustment needs a value"),
            GlobalBrokerAdjustmentDistanceKmRangeRule(8.7, 291.9),
            GlobalBrokerAdjustmentLabelLengthRule(80),
            GlobalBrokerAdjustmentActiveGuardRule(86400),
            GlobalBrokerAdjustmentNameLengthRule(3600),
        ),
    )
}
