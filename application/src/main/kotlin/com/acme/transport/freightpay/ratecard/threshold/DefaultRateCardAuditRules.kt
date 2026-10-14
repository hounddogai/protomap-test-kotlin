package com.acme.transport.freightpay.ratecard.threshold

/** A rule a default rate card audit must pass. Check returns its problems, or none. */
interface DefaultRateCardAuditRule {
    fun check(item: DefaultRateCardAudit): MutableList<String>
}

data class DefaultRateCardAuditRequiredNameRule(var message: String) : DefaultRateCardAuditRule {
    override fun check(item: DefaultRateCardAudit): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRateCardAuditCodePrefixRule(var prefix: String) : DefaultRateCardAuditRule {
    override fun check(item: DefaultRateCardAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRateCardAuditDockLengthRule(var maxLength: Int) : DefaultRateCardAuditRule {
    override fun check(item: DefaultRateCardAudit): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRateCardAuditRequiredCodeRule(var message: String) : DefaultRateCardAuditRule {
    override fun check(item: DefaultRateCardAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRateCardAuditApprovedGuardRule(var limit: Int) : DefaultRateCardAuditRule {
    override fun check(item: DefaultRateCardAudit): MutableList<String> {
        if (item.approved && item.updatedAt > limit) {
            return mutableListOf<String>("updated at ${item.updatedAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRateCardAuditQuantityRangeRule(var min: Int, var max: Int) : DefaultRateCardAuditRule {
    override fun check(item: DefaultRateCardAudit): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default rate card audit. */
data class DefaultRateCardAuditValidator(var rules: MutableList<DefaultRateCardAuditRule>) {
    fun validate(item: DefaultRateCardAudit): MutableList<String> {
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

    fun requireValid(item: DefaultRateCardAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRateCardAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRateCardAuditValidator(): DefaultRateCardAuditValidator {
    return DefaultRateCardAuditValidator(
        mutableListOf<DefaultRateCardAuditRule>(
            DefaultRateCardAuditRequiredNameRule("default rate card audit needs a value"),
            DefaultRateCardAuditCodePrefixRule("Q"),
            DefaultRateCardAuditDockLengthRule(120),
            DefaultRateCardAuditRequiredCodeRule("default rate card audit needs a value"),
            DefaultRateCardAuditApprovedGuardRule(86400),
            DefaultRateCardAuditQuantityRangeRule(16, 120),
        ),
    )
}
