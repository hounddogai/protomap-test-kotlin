package com.acme.transport.freightpay.audit.manifest

/** A rule a regional audit window must pass. Check returns its problems, or none. */
interface RegionalAuditWindowRule {
    fun check(item: RegionalAuditWindow): MutableList<String>
}

data class RegionalAuditWindowVehicleIdLengthRule(var maxLength: Int) : RegionalAuditWindowRule {
    override fun check(item: RegionalAuditWindow): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAuditWindowExternalRefLengthRule(var maxLength: Int) : RegionalAuditWindowRule {
    override fun check(item: RegionalAuditWindow): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAuditWindowRequiredCodeRule(var message: String) : RegionalAuditWindowRule {
    override fun check(item: RegionalAuditWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalAuditWindowExpiresAtRangeRule(var min: Int, var max: Int) : RegionalAuditWindowRule {
    override fun check(item: RegionalAuditWindow): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a regional audit window. */
data class RegionalAuditWindowValidator(var rules: MutableList<RegionalAuditWindowRule>) {
    fun validate(item: RegionalAuditWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalAuditWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalAuditWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalAuditWindowValidator(): RegionalAuditWindowValidator {
    return RegionalAuditWindowValidator(
        mutableListOf<RegionalAuditWindowRule>(
            RegionalAuditWindowVehicleIdLengthRule(255),
            RegionalAuditWindowExternalRefLengthRule(86400),
            RegionalAuditWindowRequiredCodeRule("regional audit window needs a value"),
            RegionalAuditWindowExpiresAtRangeRule(80, 80),
        ),
    )
}
