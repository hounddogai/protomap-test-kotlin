package com.acme.transport.safety.violation.quota

/** A rule a violation quota must pass. Check returns its problems, or none. */
interface ViolationQuotaRule {
    fun check(item: ViolationQuota): MutableList<String>
}

data class ViolationQuotaTitleLengthRule(var maxLength: Int) : ViolationQuotaRule {
    override fun check(item: ViolationQuota): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationQuotaRequiredSegmentRule(var message: String) : ViolationQuotaRule {
    override fun check(item: ViolationQuota): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationQuotaCodePrefixRule(var prefix: String) : ViolationQuotaRule {
    override fun check(item: ViolationQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ViolationQuotaRequiredTitleRule(var message: String) : ViolationQuotaRule {
    override fun check(item: ViolationQuota): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationQuotaRequiredCodeRule(var message: String) : ViolationQuotaRule {
    override fun check(item: ViolationQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationQuotaSegmentLengthRule(var maxLength: Int) : ViolationQuotaRule {
    override fun check(item: ViolationQuota): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation quota. */
data class ViolationQuotaValidator(var rules: MutableList<ViolationQuotaRule>) {
    fun validate(item: ViolationQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationQuotaValidator(): ViolationQuotaValidator {
    return ViolationQuotaValidator(
        mutableListOf<ViolationQuotaRule>(
            ViolationQuotaTitleLengthRule(80),
            ViolationQuotaRequiredSegmentRule("violation quota needs a value"),
            ViolationQuotaCodePrefixRule("REF"),
            ViolationQuotaRequiredTitleRule("violation quota needs this field"),
            ViolationQuotaRequiredCodeRule("violation quota needs an entry"),
            ViolationQuotaSegmentLengthRule(3600),
        ),
    )
}
