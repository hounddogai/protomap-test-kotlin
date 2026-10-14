package com.acme.transport.network.transittime.batch

/** A rule a transit time policy must pass. Check returns its problems, or none. */
interface TransitTimePolicyRule {
    fun check(item: TransitTimePolicy): MutableList<String>
}

data class TransitTimePolicyRequiredRouteCodeRule(var message: String) : TransitTimePolicyRule {
    override fun check(item: TransitTimePolicy): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimePolicyRouteCodeLengthRule(var maxLength: Int) : TransitTimePolicyRule {
    override fun check(item: TransitTimePolicy): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimePolicyCodeLengthRule(var maxLength: Int) : TransitTimePolicyRule {
    override fun check(item: TransitTimePolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time policy. */
data class TransitTimePolicyValidator(var rules: MutableList<TransitTimePolicyRule>) {
    fun validate(item: TransitTimePolicy): MutableList<String> {
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

    fun requireValid(item: TransitTimePolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimePolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimePolicyValidator(): TransitTimePolicyValidator {
    return TransitTimePolicyValidator(
        mutableListOf<TransitTimePolicyRule>(
            TransitTimePolicyRequiredRouteCodeRule("transit time policy needs a value"),
            TransitTimePolicyRouteCodeLengthRule(32),
            TransitTimePolicyCodeLengthRule(3600),
        ),
    )
}
