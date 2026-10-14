package com.acme.transport.freightpay.audit.auditoverride

/** A rule a tiered audit override must pass. Check returns its problems, or none. */
interface TieredAuditOverrideRule {
    fun check(item: TieredAuditOverride): MutableList<String>
}

data class TieredAuditOverrideSummaryLengthRule(var maxLength: Int) : TieredAuditOverrideRule {
    override fun check(item: TieredAuditOverride): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredAuditOverrideTimezoneLengthRule(var maxLength: Int) : TieredAuditOverrideRule {
    override fun check(item: TieredAuditOverride): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredAuditOverrideRequiredTitleRule(var message: String) : TieredAuditOverrideRule {
    override fun check(item: TieredAuditOverride): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredAuditOverrideRequiredCodeRule(var message: String) : TieredAuditOverrideRule {
    override fun check(item: TieredAuditOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredAuditOverrideTitleLengthRule(var maxLength: Int) : TieredAuditOverrideRule {
    override fun check(item: TieredAuditOverride): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered audit override. */
data class TieredAuditOverrideValidator(var rules: MutableList<TieredAuditOverrideRule>) {
    fun validate(item: TieredAuditOverride): MutableList<String> {
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

    fun requireValid(item: TieredAuditOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredAuditOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredAuditOverrideValidator(): TieredAuditOverrideValidator {
    return TieredAuditOverrideValidator(
        mutableListOf<TieredAuditOverrideRule>(
            TieredAuditOverrideSummaryLengthRule(1000),
            TieredAuditOverrideTimezoneLengthRule(255),
            TieredAuditOverrideRequiredTitleRule("tiered audit override needs this field"),
            TieredAuditOverrideRequiredCodeRule("tiered audit override needs a value"),
            TieredAuditOverrideTitleLengthRule(1000),
        ),
    )
}
