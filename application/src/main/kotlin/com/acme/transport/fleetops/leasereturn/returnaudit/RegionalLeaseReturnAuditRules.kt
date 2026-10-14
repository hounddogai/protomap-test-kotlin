package com.acme.transport.fleetops.leasereturn.returnaudit

/** A rule a regional lease return audit must pass. Check returns its problems, or none. */
interface RegionalLeaseReturnAuditRule {
    fun check(item: RegionalLeaseReturnAudit): MutableList<String>
}

data class RegionalLeaseReturnAuditCodeLengthRule(var maxLength: Int) : RegionalLeaseReturnAuditRule {
    override fun check(item: RegionalLeaseReturnAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnAuditRequiredLaneRule(var message: String) : RegionalLeaseReturnAuditRule {
    override fun check(item: RegionalLeaseReturnAudit): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnAuditCodePrefixRule(var prefix: String) : RegionalLeaseReturnAuditRule {
    override fun check(item: RegionalLeaseReturnAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnAuditRequiredCodeRule(var message: String) : RegionalLeaseReturnAuditRule {
    override fun check(item: RegionalLeaseReturnAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnAuditGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : RegionalLeaseReturnAuditRule {
    override fun check(item: RegionalLeaseReturnAudit): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class RegionalLeaseReturnAuditTrailerIdLengthRule(var maxLength: Int) : RegionalLeaseReturnAuditRule {
    override fun check(item: RegionalLeaseReturnAudit): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional lease return audit. */
data class RegionalLeaseReturnAuditValidator(var rules: MutableList<RegionalLeaseReturnAuditRule>) {
    fun validate(item: RegionalLeaseReturnAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalLeaseReturnAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalLeaseReturnAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalLeaseReturnAuditValidator(): RegionalLeaseReturnAuditValidator {
    return RegionalLeaseReturnAuditValidator(
        mutableListOf<RegionalLeaseReturnAuditRule>(
            RegionalLeaseReturnAuditCodeLengthRule(1000),
            RegionalLeaseReturnAuditRequiredLaneRule("regional lease return audit needs this field"),
            RegionalLeaseReturnAuditCodePrefixRule("ACM"),
            RegionalLeaseReturnAuditRequiredCodeRule("regional lease return audit needs an entry"),
            RegionalLeaseReturnAuditGraceDaysRangeRule(32, 1000),
            RegionalLeaseReturnAuditTrailerIdLengthRule(86400),
        ),
    )
}
