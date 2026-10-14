package com.acme.transport.terminal.crossdock.snapshot

/** A rule a cross dock quota must pass. Check returns its problems, or none. */
interface CrossDockQuotaRule {
    fun check(item: CrossDockQuota): MutableList<String>
}

data class CrossDockQuotaRequiredCodeRule(var message: String) : CrossDockQuotaRule {
    override fun check(item: CrossDockQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockQuotaActiveGuardRule(var limit: Double) : CrossDockQuotaRule {
    override fun check(item: CrossDockQuota): MutableList<String> {
        if (item.active && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockQuotaRequiredNameRule(var message: String) : CrossDockQuotaRule {
    override fun check(item: CrossDockQuota): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock quota. */
data class CrossDockQuotaValidator(var rules: MutableList<CrossDockQuotaRule>) {
    fun validate(item: CrossDockQuota): MutableList<String> {
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

    fun requireValid(item: CrossDockQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockQuotaValidator(): CrossDockQuotaValidator {
    return CrossDockQuotaValidator(
        mutableListOf<CrossDockQuotaRule>(
            CrossDockQuotaRequiredCodeRule("cross dock quota needs a value"),
            CrossDockQuotaActiveGuardRule(8.3),
            CrossDockQuotaRequiredNameRule("cross dock quota needs a value"),
        ),
    )
}
