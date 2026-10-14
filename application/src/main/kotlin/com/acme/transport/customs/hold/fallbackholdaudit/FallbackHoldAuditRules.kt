package com.acme.transport.customs.hold.fallbackholdaudit

/** A rule a fallback hold audit must pass. Check returns its problems, or none. */
interface FallbackHoldAuditRule {
    fun check(item: FallbackHoldAudit): MutableList<String>
}

data class FallbackHoldAuditTitleLengthRule(var maxLength: Int) : FallbackHoldAuditRule {
    override fun check(item: FallbackHoldAudit): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackHoldAuditVersionRangeRule(var min: Int, var max: Int) : FallbackHoldAuditRule {
    override fun check(item: FallbackHoldAudit): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackHoldAuditVehicleIdLengthRule(var maxLength: Int) : FallbackHoldAuditRule {
    override fun check(item: FallbackHoldAudit): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackHoldAuditPalletsRangeRule(var min: Int, var max: Int) : FallbackHoldAuditRule {
    override fun check(item: FallbackHoldAudit): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class FallbackHoldAuditRequiredTitleRule(var message: String) : FallbackHoldAuditRule {
    override fun check(item: FallbackHoldAudit): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback hold audit. */
data class FallbackHoldAuditValidator(var rules: MutableList<FallbackHoldAuditRule>) {
    fun validate(item: FallbackHoldAudit): MutableList<String> {
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

    fun requireValid(item: FallbackHoldAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackHoldAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackHoldAuditValidator(): FallbackHoldAuditValidator {
    return FallbackHoldAuditValidator(
        mutableListOf<FallbackHoldAuditRule>(
            FallbackHoldAuditTitleLengthRule(86400),
            FallbackHoldAuditVersionRangeRule(86400, 86400),
            FallbackHoldAuditVehicleIdLengthRule(86400),
            FallbackHoldAuditPalletsRangeRule(16, 86400),
            FallbackHoldAuditRequiredTitleRule("fallback hold audit needs an entry"),
        ),
    )
}
