package com.acme.transport.fleetops.defect.defectcheckpoint

import com.acme.transport.kit.text.normalize

/** A rule a tiered defect audit must pass. Check returns its problems, or none. */
interface TieredDefectAuditRule {
    fun check(item: TieredDefectAudit): MutableList<String>
}

data class TieredDefectAuditAllowedCategoryRule(var allowed: MutableList<String>) : TieredDefectAuditRule {
    override fun check(item: TieredDefectAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class TieredDefectAuditRequiredCategoryRule(var message: String) : TieredDefectAuditRule {
    override fun check(item: TieredDefectAudit): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDefectAuditThresholdRangeRule(var min: Double, var max: Double) : TieredDefectAuditRule {
    override fun check(item: TieredDefectAudit): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredDefectAuditCodePrefixRule(var prefix: String) : TieredDefectAuditRule {
    override fun check(item: TieredDefectAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredDefectAuditCategoryLengthRule(var maxLength: Int) : TieredDefectAuditRule {
    override fun check(item: TieredDefectAudit): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredDefectAuditRequiredOwnerIdRule(var message: String) : TieredDefectAuditRule {
    override fun check(item: TieredDefectAudit): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered defect audit. */
data class TieredDefectAuditValidator(var rules: MutableList<TieredDefectAuditRule>) {
    fun validate(item: TieredDefectAudit): MutableList<String> {
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

    fun requireValid(item: TieredDefectAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDefectAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDefectAuditValidator(): TieredDefectAuditValidator {
    return TieredDefectAuditValidator(
        mutableListOf<TieredDefectAuditRule>(
            TieredDefectAuditAllowedCategoryRule(mutableListOf<String>("regional", "primary")),
            TieredDefectAuditRequiredCategoryRule("tiered defect audit needs this field"),
            TieredDefectAuditThresholdRangeRule(0.8, 3747.1),
            TieredDefectAuditCodePrefixRule("ACM"),
            TieredDefectAuditCategoryLengthRule(16),
            TieredDefectAuditRequiredOwnerIdRule("tiered defect audit needs an entry"),
        ),
    )
}
