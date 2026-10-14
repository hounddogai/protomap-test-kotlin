package com.acme.transport.freightpay.audit.tier

/** A rule a tiered audit tier must pass. Check returns its problems, or none. */
interface TieredAuditTierRule {
    fun check(item: TieredAuditTier): MutableList<String>
}

data class TieredAuditTierVersionRangeRule(var min: Int, var max: Int) : TieredAuditTierRule {
    override fun check(item: TieredAuditTier): MutableList<String> {
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

data class TieredAuditTierCodeLengthRule(var maxLength: Int) : TieredAuditTierRule {
    override fun check(item: TieredAuditTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredAuditTierRequiredCodeRule(var message: String) : TieredAuditTierRule {
    override fun check(item: TieredAuditTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered audit tier. */
data class TieredAuditTierValidator(var rules: MutableList<TieredAuditTierRule>) {
    fun validate(item: TieredAuditTier): MutableList<String> {
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

    fun requireValid(item: TieredAuditTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredAuditTier>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredAuditTierValidator(): TieredAuditTierValidator {
    return TieredAuditTierValidator(
        mutableListOf<TieredAuditTierRule>(
            TieredAuditTierVersionRangeRule(80, 255),
            TieredAuditTierCodeLengthRule(32),
            TieredAuditTierRequiredCodeRule("tiered audit tier needs this field"),
        ),
    )
}
