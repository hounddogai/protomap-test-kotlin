package com.acme.transport.fleetops.utilization.reservation

/** A rule a utilization baseline must pass. Check returns its problems, or none. */
interface UtilizationBaselineRule {
    fun check(item: UtilizationBaseline): MutableList<String>
}

data class UtilizationBaselineRequiredCodeRule(var message: String) : UtilizationBaselineRule {
    override fun check(item: UtilizationBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationBaselineCodePrefixRule(var prefix: String) : UtilizationBaselineRule {
    override fun check(item: UtilizationBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationBaselineRequiredNameRule(var message: String) : UtilizationBaselineRule {
    override fun check(item: UtilizationBaseline): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationBaselineNameLengthRule(var maxLength: Int) : UtilizationBaselineRule {
    override fun check(item: UtilizationBaseline): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationBaselineCodeLengthRule(var maxLength: Int) : UtilizationBaselineRule {
    override fun check(item: UtilizationBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationBaselineApprovedGuardRule(var limit: Int) : UtilizationBaselineRule {
    override fun check(item: UtilizationBaseline): MutableList<String> {
        if (item.approved && item.expiresAt > limit) {
            return mutableListOf<String>("expires at ${item.expiresAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization baseline. */
data class UtilizationBaselineValidator(var rules: MutableList<UtilizationBaselineRule>) {
    fun validate(item: UtilizationBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationBaselineValidator(): UtilizationBaselineValidator {
    return UtilizationBaselineValidator(
        mutableListOf<UtilizationBaselineRule>(
            UtilizationBaselineRequiredCodeRule("utilization baseline needs an entry"),
            UtilizationBaselineCodePrefixRule("Q"),
            UtilizationBaselineRequiredNameRule("utilization baseline needs a value"),
            UtilizationBaselineNameLengthRule(3600),
            UtilizationBaselineCodeLengthRule(64),
            UtilizationBaselineApprovedGuardRule(3600),
        ),
    )
}
