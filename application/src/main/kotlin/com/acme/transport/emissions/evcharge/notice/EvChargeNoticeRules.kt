package com.acme.transport.emissions.evcharge.notice

/** A rule a ev charge notice must pass. Check returns its problems, or none. */
interface EvChargeNoticeRule {
    fun check(item: EvChargeNotice): MutableList<String>
}

data class EvChargeNoticeCodePrefixRule(var prefix: String) : EvChargeNoticeRule {
    override fun check(item: EvChargeNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeNoticeCodeLengthRule(var maxLength: Int) : EvChargeNoticeRule {
    override fun check(item: EvChargeNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeNoticeArchivedGuardRule(var limit: Double) : EvChargeNoticeRule {
    override fun check(item: EvChargeNotice): MutableList<String> {
        if (item.archived && item.ceiling > limit) {
            return mutableListOf<String>("ceiling ${item.ceiling} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge notice. */
data class EvChargeNoticeValidator(var rules: MutableList<EvChargeNoticeRule>) {
    fun validate(item: EvChargeNotice): MutableList<String> {
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

    fun requireValid(item: EvChargeNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeNoticeValidator(): EvChargeNoticeValidator {
    return EvChargeNoticeValidator(
        mutableListOf<EvChargeNoticeRule>(
            EvChargeNoticeCodePrefixRule("ACM"),
            EvChargeNoticeCodeLengthRule(64),
            EvChargeNoticeArchivedGuardRule(6.0),
        ),
    )
}
