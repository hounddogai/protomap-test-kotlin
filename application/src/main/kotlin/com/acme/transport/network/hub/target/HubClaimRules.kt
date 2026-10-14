package com.acme.transport.network.hub.target

/** A rule a hub claim must pass. Check returns its problems, or none. */
interface HubClaimRule {
    fun check(item: HubClaim): MutableList<String>
}

data class HubClaimLimitRangeRule(var min: Int, var max: Int) : HubClaimRule {
    override fun check(item: HubClaim): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class HubClaimRequiredUnitRule(var message: String) : HubClaimRule {
    override fun check(item: HubClaim): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubClaimRequiredCodeRule(var message: String) : HubClaimRule {
    override fun check(item: HubClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubClaimRequiredSummaryRule(var message: String) : HubClaimRule {
    override fun check(item: HubClaim): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubClaimUnitLengthRule(var maxLength: Int) : HubClaimRule {
    override fun check(item: HubClaim): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubClaimSummaryLengthRule(var maxLength: Int) : HubClaimRule {
    override fun check(item: HubClaim): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub claim. */
data class HubClaimValidator(var rules: MutableList<HubClaimRule>) {
    fun validate(item: HubClaim): MutableList<String> {
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

    fun requireValid(item: HubClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultHubClaimValidator(): HubClaimValidator {
    return HubClaimValidator(
        mutableListOf<HubClaimRule>(
            HubClaimLimitRangeRule(32, 80),
            HubClaimRequiredUnitRule("hub claim needs a value"),
            HubClaimRequiredCodeRule("hub claim needs a value"),
            HubClaimRequiredSummaryRule("hub claim needs a value"),
            HubClaimUnitLengthRule(86400),
            HubClaimSummaryLengthRule(86400),
        ),
    )
}
