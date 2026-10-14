package com.acme.transport.freightpay.audit.bundle

/** A rule a audit bundle must pass. Check returns its problems, or none. */
interface AuditBundleRule {
    fun check(item: AuditBundle): MutableList<String>
}

data class AuditBundleRequiredCodeRule(var message: String) : AuditBundleRule {
    override fun check(item: AuditBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditBundleCodeLengthRule(var maxLength: Int) : AuditBundleRule {
    override fun check(item: AuditBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditBundleLabelLengthRule(var maxLength: Int) : AuditBundleRule {
    override fun check(item: AuditBundle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditBundleRequiredContactEmailRule(var message: String) : AuditBundleRule {
    override fun check(item: AuditBundle): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditBundleRequiredSourceRule(var message: String) : AuditBundleRule {
    override fun check(item: AuditBundle): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditBundleAmountRangeRule(var min: Double, var max: Double) : AuditBundleRule {
    override fun check(item: AuditBundle): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a audit bundle. */
data class AuditBundleValidator(var rules: MutableList<AuditBundleRule>) {
    fun validate(item: AuditBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditBundleValidator(): AuditBundleValidator {
    return AuditBundleValidator(
        mutableListOf<AuditBundleRule>(
            AuditBundleRequiredCodeRule("audit bundle needs a value"),
            AuditBundleCodeLengthRule(3600),
            AuditBundleLabelLengthRule(32),
            AuditBundleRequiredContactEmailRule("audit bundle needs this field"),
            AuditBundleRequiredSourceRule("audit bundle needs this field"),
            AuditBundleAmountRangeRule(6.4, 1627.5),
        ),
    )
}
