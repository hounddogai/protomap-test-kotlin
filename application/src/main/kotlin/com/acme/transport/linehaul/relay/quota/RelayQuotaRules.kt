package com.acme.transport.linehaul.relay.quota

/** A rule a relay quota must pass. Check returns its problems, or none. */
interface RelayQuotaRule {
    fun check(item: RelayQuota): MutableList<String>
}

data class RelayQuotaCodeLengthRule(var maxLength: Int) : RelayQuotaRule {
    override fun check(item: RelayQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayQuotaNameLengthRule(var maxLength: Int) : RelayQuotaRule {
    override fun check(item: RelayQuota): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayQuotaRequiredSegmentRule(var message: String) : RelayQuotaRule {
    override fun check(item: RelayQuota): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayQuotaCodePrefixRule(var prefix: String) : RelayQuotaRule {
    override fun check(item: RelayQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RelayQuotaRequiredNameRule(var message: String) : RelayQuotaRule {
    override fun check(item: RelayQuota): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayQuotaExpiresAtRangeRule(var min: Int, var max: Int) : RelayQuotaRule {
    override fun check(item: RelayQuota): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a relay quota. */
data class RelayQuotaValidator(var rules: MutableList<RelayQuotaRule>) {
    fun validate(item: RelayQuota): MutableList<String> {
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

    fun requireValid(item: RelayQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayQuotaValidator(): RelayQuotaValidator {
    return RelayQuotaValidator(
        mutableListOf<RelayQuotaRule>(
            RelayQuotaCodeLengthRule(120),
            RelayQuotaNameLengthRule(64),
            RelayQuotaRequiredSegmentRule("relay quota needs an entry"),
            RelayQuotaCodePrefixRule("OPS"),
            RelayQuotaRequiredNameRule("relay quota needs an entry"),
            RelayQuotaExpiresAtRangeRule(64, 1000),
        ),
    )
}
