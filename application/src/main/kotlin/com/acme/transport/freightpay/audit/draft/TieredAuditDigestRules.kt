package com.acme.transport.freightpay.audit.draft

/** A rule a tiered audit digest must pass. Check returns its problems, or none. */
interface TieredAuditDigestRule {
    fun check(item: TieredAuditDigest): MutableList<String>
}

data class TieredAuditDigestCodeLengthRule(var maxLength: Int) : TieredAuditDigestRule {
    override fun check(item: TieredAuditDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredAuditDigestGraceDaysRangeRule(var min: Int, var max: Int) : TieredAuditDigestRule {
    override fun check(item: TieredAuditDigest): MutableList<String> {
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

data class TieredAuditDigestNotesLengthRule(var maxLength: Int) : TieredAuditDigestRule {
    override fun check(item: TieredAuditDigest): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredAuditDigestRequiredCodeRule(var message: String) : TieredAuditDigestRule {
    override fun check(item: TieredAuditDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered audit digest. */
data class TieredAuditDigestValidator(var rules: MutableList<TieredAuditDigestRule>) {
    fun validate(item: TieredAuditDigest): MutableList<String> {
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

    fun requireValid(item: TieredAuditDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredAuditDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredAuditDigestValidator(): TieredAuditDigestValidator {
    return TieredAuditDigestValidator(
        mutableListOf<TieredAuditDigestRule>(
            TieredAuditDigestCodeLengthRule(64),
            TieredAuditDigestGraceDaysRangeRule(255, 3600),
            TieredAuditDigestNotesLengthRule(86400),
            TieredAuditDigestRequiredCodeRule("tiered audit digest needs a value"),
        ),
    )
}
