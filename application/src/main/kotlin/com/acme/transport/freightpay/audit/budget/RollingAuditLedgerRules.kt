package com.acme.transport.freightpay.audit.budget

/** A rule a rolling audit ledger must pass. Check returns its problems, or none. */
interface RollingAuditLedgerRule {
    fun check(item: RollingAuditLedger): MutableList<String>
}

data class RollingAuditLedgerCodeLengthRule(var maxLength: Int) : RollingAuditLedgerRule {
    override fun check(item: RollingAuditLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingAuditLedgerTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RollingAuditLedgerRule {
    override fun check(item: RollingAuditLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class RollingAuditLedgerCodePrefixRule(var prefix: String) : RollingAuditLedgerRule {
    override fun check(item: RollingAuditLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingAuditLedgerRequiredSourceRule(var message: String) : RollingAuditLedgerRule {
    override fun check(item: RollingAuditLedger): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingAuditLedgerMarginRangeRule(var min: Double, var max: Double) : RollingAuditLedgerRule {
    override fun check(item: RollingAuditLedger): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rolling audit ledger. */
data class RollingAuditLedgerValidator(var rules: MutableList<RollingAuditLedgerRule>) {
    fun validate(item: RollingAuditLedger): MutableList<String> {
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

    fun requireValid(item: RollingAuditLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingAuditLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingAuditLedgerValidator(): RollingAuditLedgerValidator {
    return RollingAuditLedgerValidator(
        mutableListOf<RollingAuditLedgerRule>(
            RollingAuditLedgerCodeLengthRule(80),
            RollingAuditLedgerTagsLimitRule(1000, mutableListOf<String>()),
            RollingAuditLedgerCodePrefixRule("REF"),
            RollingAuditLedgerRequiredSourceRule("rolling audit ledger needs an entry"),
            RollingAuditLedgerMarginRangeRule(4.4, 1884.4),
        ),
    )
}
