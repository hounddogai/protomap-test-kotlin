package com.acme.transport.emissions.fuelburn.pendingburnthreshold

/** A rule a regional fuel burn audit must pass. Check returns its problems, or none. */
interface RegionalFuelBurnAuditRule {
    fun check(item: RegionalFuelBurnAudit): MutableList<String>
}

data class RegionalFuelBurnAuditRequiredTimezoneRule(var message: String) : RegionalFuelBurnAuditRule {
    override fun check(item: RegionalFuelBurnAudit): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnAuditVisibleGuardRule(var limit: Int) : RegionalFuelBurnAuditRule {
    override fun check(item: RegionalFuelBurnAudit): MutableList<String> {
        if (item.visible && item.retries > limit) {
            return mutableListOf<String>("retries ${item.retries} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnAuditDiscountRangeRule(
    var min: Double,
    var max: Double,
) : RegionalFuelBurnAuditRule {
    override fun check(item: RegionalFuelBurnAudit): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class RegionalFuelBurnAuditRetriesRangeRule(var min: Int, var max: Int) : RegionalFuelBurnAuditRule {
    override fun check(item: RegionalFuelBurnAudit): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class RegionalFuelBurnAuditExternalRefLengthRule(var maxLength: Int) : RegionalFuelBurnAuditRule {
    override fun check(item: RegionalFuelBurnAudit): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnAuditLabelLengthRule(var maxLength: Int) : RegionalFuelBurnAuditRule {
    override fun check(item: RegionalFuelBurnAudit): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional fuel burn audit. */
data class RegionalFuelBurnAuditValidator(var rules: MutableList<RegionalFuelBurnAuditRule>) {
    fun validate(item: RegionalFuelBurnAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalFuelBurnAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalFuelBurnAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalFuelBurnAuditValidator(): RegionalFuelBurnAuditValidator {
    return RegionalFuelBurnAuditValidator(
        mutableListOf<RegionalFuelBurnAuditRule>(
            RegionalFuelBurnAuditRequiredTimezoneRule("regional fuel burn audit needs an entry"),
            RegionalFuelBurnAuditVisibleGuardRule(86400),
            RegionalFuelBurnAuditDiscountRangeRule(1.0, 4845.3),
            RegionalFuelBurnAuditRetriesRangeRule(3600, 86400),
            RegionalFuelBurnAuditExternalRefLengthRule(1000),
            RegionalFuelBurnAuditLabelLengthRule(1000),
        ),
    )
}
