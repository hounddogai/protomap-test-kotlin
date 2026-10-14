package com.acme.transport.freightpay.audit.policy

/** A rule a regional audit bundle must pass. Check returns its problems, or none. */
interface RegionalAuditBundleRule {
    fun check(item: RegionalAuditBundle): MutableList<String>
}

data class RegionalAuditBundleApprovedGuardRule(var limit: Double) : RegionalAuditBundleRule {
    override fun check(item: RegionalAuditBundle): MutableList<String> {
        if (item.approved && item.distanceKm > limit) {
            return mutableListOf<String>("distance km ${item.distanceKm} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAuditBundleRequiredChecksumRule(var message: String) : RegionalAuditBundleRule {
    override fun check(item: RegionalAuditBundle): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalAuditBundleDiscountRangeRule(var min: Double, var max: Double) : RegionalAuditBundleRule {
    override fun check(item: RegionalAuditBundle): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional audit bundle. */
data class RegionalAuditBundleValidator(var rules: MutableList<RegionalAuditBundleRule>) {
    fun validate(item: RegionalAuditBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalAuditBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalAuditBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalAuditBundleValidator(): RegionalAuditBundleValidator {
    return RegionalAuditBundleValidator(
        mutableListOf<RegionalAuditBundleRule>(
            RegionalAuditBundleApprovedGuardRule(1.8),
            RegionalAuditBundleRequiredChecksumRule("regional audit bundle needs a value"),
            RegionalAuditBundleDiscountRangeRule(2.8, 1767.3),
        ),
    )
}
