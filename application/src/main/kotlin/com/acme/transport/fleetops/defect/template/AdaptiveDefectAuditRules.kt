package com.acme.transport.fleetops.defect.template

/** A rule a adaptive defect audit must pass. Check returns its problems, or none. */
interface AdaptiveDefectAuditRule {
    fun check(item: AdaptiveDefectAudit): MutableList<String>
}

data class AdaptiveDefectAuditCodeLengthRule(var maxLength: Int) : AdaptiveDefectAuditRule {
    override fun check(item: AdaptiveDefectAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDefectAuditLoadKgRangeRule(var min: Double, var max: Double) : AdaptiveDefectAuditRule {
    override fun check(item: AdaptiveDefectAudit): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class AdaptiveDefectAuditActiveGuardRule(var limit: Int) : AdaptiveDefectAuditRule {
    override fun check(item: AdaptiveDefectAudit): MutableList<String> {
        if (item.active && item.updatedAt > limit) {
            return mutableListOf<String>("updated at ${item.updatedAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDefectAuditRequiredRouteCodeRule(var message: String) : AdaptiveDefectAuditRule {
    override fun check(item: AdaptiveDefectAudit): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDefectAuditRouteCodeLengthRule(var maxLength: Int) : AdaptiveDefectAuditRule {
    override fun check(item: AdaptiveDefectAudit): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive defect audit. */
data class AdaptiveDefectAuditValidator(var rules: MutableList<AdaptiveDefectAuditRule>) {
    fun validate(item: AdaptiveDefectAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveDefectAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveDefectAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveDefectAuditValidator(): AdaptiveDefectAuditValidator {
    return AdaptiveDefectAuditValidator(
        mutableListOf<AdaptiveDefectAuditRule>(
            AdaptiveDefectAuditCodeLengthRule(64),
            AdaptiveDefectAuditLoadKgRangeRule(0.2, 2873.1),
            AdaptiveDefectAuditActiveGuardRule(80),
            AdaptiveDefectAuditRequiredRouteCodeRule("adaptive defect audit needs an entry"),
            AdaptiveDefectAuditRouteCodeLengthRule(255),
        ),
    )
}
