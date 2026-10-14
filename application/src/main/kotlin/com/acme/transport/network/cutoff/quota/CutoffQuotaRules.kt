package com.acme.transport.network.cutoff.quota

/** A rule a cutoff quota must pass. Check returns its problems, or none. */
interface CutoffQuotaRule {
    fun check(item: CutoffQuota): MutableList<String>
}

data class CutoffQuotaCodePrefixRule(var prefix: String) : CutoffQuotaRule {
    override fun check(item: CutoffQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CutoffQuotaRevisionRangeRule(var min: Int, var max: Int) : CutoffQuotaRule {
    override fun check(item: CutoffQuota): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class CutoffQuotaRequiredCodeRule(var message: String) : CutoffQuotaRule {
    override fun check(item: CutoffQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff quota. */
data class CutoffQuotaValidator(var rules: MutableList<CutoffQuotaRule>) {
    fun validate(item: CutoffQuota): MutableList<String> {
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

    fun requireValid(item: CutoffQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffQuotaValidator(): CutoffQuotaValidator {
    return CutoffQuotaValidator(
        mutableListOf<CutoffQuotaRule>(
            CutoffQuotaCodePrefixRule("ACM"),
            CutoffQuotaRevisionRangeRule(16, 86400),
            CutoffQuotaRequiredCodeRule("cutoff quota needs this field"),
        ),
    )
}
