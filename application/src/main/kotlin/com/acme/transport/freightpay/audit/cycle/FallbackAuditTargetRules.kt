package com.acme.transport.freightpay.audit.cycle

/** A rule a fallback audit target must pass. Check returns its problems, or none. */
interface FallbackAuditTargetRule {
    fun check(item: FallbackAuditTarget): MutableList<String>
}

data class FallbackAuditTargetCodePrefixRule(var prefix: String) : FallbackAuditTargetRule {
    override fun check(item: FallbackAuditTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAuditTargetRequiredNameRule(var message: String) : FallbackAuditTargetRule {
    override fun check(item: FallbackAuditTarget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackAuditTargetGraceDaysRangeRule(var min: Int, var max: Int) : FallbackAuditTargetRule {
    override fun check(item: FallbackAuditTarget): MutableList<String> {
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

/** Runs every rule of a fallback audit target. */
data class FallbackAuditTargetValidator(var rules: MutableList<FallbackAuditTargetRule>) {
    fun validate(item: FallbackAuditTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackAuditTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackAuditTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackAuditTargetValidator(): FallbackAuditTargetValidator {
    return FallbackAuditTargetValidator(
        mutableListOf<FallbackAuditTargetRule>(
            FallbackAuditTargetCodePrefixRule("REF"),
            FallbackAuditTargetRequiredNameRule("fallback audit target needs a value"),
            FallbackAuditTargetGraceDaysRangeRule(80, 255),
        ),
    )
}
