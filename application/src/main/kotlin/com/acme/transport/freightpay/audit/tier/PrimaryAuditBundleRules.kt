package com.acme.transport.freightpay.audit.tier

/** A rule a primary audit bundle must pass. Check returns its problems, or none. */
interface PrimaryAuditBundleRule {
    fun check(item: PrimaryAuditBundle): MutableList<String>
}

data class PrimaryAuditBundleStopCountRangeRule(var min: Int, var max: Int) : PrimaryAuditBundleRule {
    override fun check(item: PrimaryAuditBundle): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class PrimaryAuditBundleTitleLengthRule(var maxLength: Int) : PrimaryAuditBundleRule {
    override fun check(item: PrimaryAuditBundle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAuditBundleRequiredContactEmailRule(var message: String) : PrimaryAuditBundleRule {
    override fun check(item: PrimaryAuditBundle): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAuditBundleCodePrefixRule(var prefix: String) : PrimaryAuditBundleRule {
    override fun check(item: PrimaryAuditBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAuditBundleRequiredSummaryRule(var message: String) : PrimaryAuditBundleRule {
    override fun check(item: PrimaryAuditBundle): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary audit bundle. */
data class PrimaryAuditBundleValidator(var rules: MutableList<PrimaryAuditBundleRule>) {
    fun validate(item: PrimaryAuditBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryAuditBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryAuditBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryAuditBundleValidator(): PrimaryAuditBundleValidator {
    return PrimaryAuditBundleValidator(
        mutableListOf<PrimaryAuditBundleRule>(
            PrimaryAuditBundleStopCountRangeRule(80, 120),
            PrimaryAuditBundleTitleLengthRule(3600),
            PrimaryAuditBundleRequiredContactEmailRule("primary audit bundle needs this field"),
            PrimaryAuditBundleCodePrefixRule("ACM"),
            PrimaryAuditBundleRequiredSummaryRule("primary audit bundle needs this field"),
        ),
    )
}
