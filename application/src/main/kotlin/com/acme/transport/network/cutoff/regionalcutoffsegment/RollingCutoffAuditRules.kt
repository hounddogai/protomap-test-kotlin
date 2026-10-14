package com.acme.transport.network.cutoff.regionalcutoffsegment

/** A rule a rolling cutoff audit must pass. Check returns its problems, or none. */
interface RollingCutoffAuditRule {
    fun check(item: RollingCutoffAudit): MutableList<String>
}

data class RollingCutoffAuditCodeLengthRule(var maxLength: Int) : RollingCutoffAuditRule {
    override fun check(item: RollingCutoffAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffAuditRequiredLocaleRule(var message: String) : RollingCutoffAuditRule {
    override fun check(item: RollingCutoffAudit): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffAuditVersionRangeRule(var min: Int, var max: Int) : RollingCutoffAuditRule {
    override fun check(item: RollingCutoffAudit): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class RollingCutoffAuditSourceLengthRule(var maxLength: Int) : RollingCutoffAuditRule {
    override fun check(item: RollingCutoffAudit): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffAuditVisibleGuardRule(var limit: Int) : RollingCutoffAuditRule {
    override fun check(item: RollingCutoffAudit): MutableList<String> {
        if (item.visible && item.version > limit) {
            return mutableListOf<String>("version ${item.version} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class RollingCutoffAuditCodePrefixRule(var prefix: String) : RollingCutoffAuditRule {
    override fun check(item: RollingCutoffAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling cutoff audit. */
data class RollingCutoffAuditValidator(var rules: MutableList<RollingCutoffAuditRule>) {
    fun validate(item: RollingCutoffAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingCutoffAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingCutoffAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingCutoffAuditValidator(): RollingCutoffAuditValidator {
    return RollingCutoffAuditValidator(
        mutableListOf<RollingCutoffAuditRule>(
            RollingCutoffAuditCodeLengthRule(64),
            RollingCutoffAuditRequiredLocaleRule("rolling cutoff audit needs a value"),
            RollingCutoffAuditVersionRangeRule(32, 120),
            RollingCutoffAuditSourceLengthRule(3600),
            RollingCutoffAuditVisibleGuardRule(120),
            RollingCutoffAuditCodePrefixRule("ACM"),
        ),
    )
}
