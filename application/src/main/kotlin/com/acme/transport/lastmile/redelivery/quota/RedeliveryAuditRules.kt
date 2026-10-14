package com.acme.transport.lastmile.redelivery.quota

/** A rule a redelivery audit must pass. Check returns its problems, or none. */
interface RedeliveryAuditRule {
    fun check(item: RedeliveryAudit): MutableList<String>
}

data class RedeliveryAuditRequiredRouteCodeRule(var message: String) : RedeliveryAuditRule {
    override fun check(item: RedeliveryAudit): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryAuditCodeLengthRule(var maxLength: Int) : RedeliveryAuditRule {
    override fun check(item: RedeliveryAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryAuditCodePrefixRule(var prefix: String) : RedeliveryAuditRule {
    override fun check(item: RedeliveryAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryAuditVisibleGuardRule(var limit: Double) : RedeliveryAuditRule {
    override fun check(item: RedeliveryAudit): MutableList<String> {
        if (item.visible && item.rate > limit) {
            return mutableListOf<String>("rate ${item.rate} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery audit. */
data class RedeliveryAuditValidator(var rules: MutableList<RedeliveryAuditRule>) {
    fun validate(item: RedeliveryAudit): MutableList<String> {
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

    fun requireValid(item: RedeliveryAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryAuditValidator(): RedeliveryAuditValidator {
    return RedeliveryAuditValidator(
        mutableListOf<RedeliveryAuditRule>(
            RedeliveryAuditRequiredRouteCodeRule("redelivery audit needs this field"),
            RedeliveryAuditCodeLengthRule(86400),
            RedeliveryAuditCodePrefixRule("OPS"),
            RedeliveryAuditVisibleGuardRule(3.5),
        ),
    )
}
