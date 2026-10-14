package com.acme.transport.crewing.availability.availabilityaudit

/** A rule a adaptive availability audit must pass. Check returns its problems, or none. */
interface AdaptiveAvailabilityAuditRule {
    fun check(item: AdaptiveAvailabilityAudit): MutableList<String>
}

data class AdaptiveAvailabilityAuditCodePrefixRule(var prefix: String) : AdaptiveAvailabilityAuditRule {
    override fun check(item: AdaptiveAvailabilityAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAvailabilityAuditRequiredRegionRule(var message: String) : AdaptiveAvailabilityAuditRule {
    override fun check(item: AdaptiveAvailabilityAudit): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAvailabilityAuditVisibleGuardRule(var limit: Double) : AdaptiveAvailabilityAuditRule {
    override fun check(item: AdaptiveAvailabilityAudit): MutableList<String> {
        if (item.visible && item.ceiling > limit) {
            return mutableListOf<String>("ceiling ${item.ceiling} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAvailabilityAuditCodeLengthRule(var maxLength: Int) : AdaptiveAvailabilityAuditRule {
    override fun check(item: AdaptiveAvailabilityAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAvailabilityAuditQuantityRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveAvailabilityAuditRule {
    override fun check(item: AdaptiveAvailabilityAudit): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive availability audit. */
data class AdaptiveAvailabilityAuditValidator(var rules: MutableList<AdaptiveAvailabilityAuditRule>) {
    fun validate(item: AdaptiveAvailabilityAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveAvailabilityAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveAvailabilityAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveAvailabilityAuditValidator(): AdaptiveAvailabilityAuditValidator {
    return AdaptiveAvailabilityAuditValidator(
        mutableListOf<AdaptiveAvailabilityAuditRule>(
            AdaptiveAvailabilityAuditCodePrefixRule("REF"),
            AdaptiveAvailabilityAuditRequiredRegionRule("adaptive availability audit needs a value"),
            AdaptiveAvailabilityAuditVisibleGuardRule(2.7),
            AdaptiveAvailabilityAuditCodeLengthRule(255),
            AdaptiveAvailabilityAuditQuantityRangeRule(80, 1000),
        ),
    )
}
