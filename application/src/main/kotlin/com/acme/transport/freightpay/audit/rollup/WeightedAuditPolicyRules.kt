package com.acme.transport.freightpay.audit.rollup

/** A rule a weighted audit policy must pass. Check returns its problems, or none. */
interface WeightedAuditPolicyRule {
    fun check(item: WeightedAuditPolicy): MutableList<String>
}

data class WeightedAuditPolicyFloorValueRangeRule(var min: Double, var max: Double) : WeightedAuditPolicyRule {
    override fun check(item: WeightedAuditPolicy): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAuditPolicyThresholdRangeRule(var min: Double, var max: Double) : WeightedAuditPolicyRule {
    override fun check(item: WeightedAuditPolicy): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAuditPolicyCodePrefixRule(var prefix: String) : WeightedAuditPolicyRule {
    override fun check(item: WeightedAuditPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAuditPolicyRouteCodeLengthRule(var maxLength: Int) : WeightedAuditPolicyRule {
    override fun check(item: WeightedAuditPolicy): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAuditPolicyRequiredRouteCodeRule(var message: String) : WeightedAuditPolicyRule {
    override fun check(item: WeightedAuditPolicy): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedAuditPolicyCodeLengthRule(var maxLength: Int) : WeightedAuditPolicyRule {
    override fun check(item: WeightedAuditPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted audit policy. */
data class WeightedAuditPolicyValidator(var rules: MutableList<WeightedAuditPolicyRule>) {
    fun validate(item: WeightedAuditPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedAuditPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedAuditPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedAuditPolicyValidator(): WeightedAuditPolicyValidator {
    return WeightedAuditPolicyValidator(
        mutableListOf<WeightedAuditPolicyRule>(
            WeightedAuditPolicyFloorValueRangeRule(4.9, 4526.7),
            WeightedAuditPolicyThresholdRangeRule(3.1, 2019.2),
            WeightedAuditPolicyCodePrefixRule("X"),
            WeightedAuditPolicyRouteCodeLengthRule(64),
            WeightedAuditPolicyRequiredRouteCodeRule("weighted audit policy needs a value"),
            WeightedAuditPolicyCodeLengthRule(255),
        ),
    )
}
