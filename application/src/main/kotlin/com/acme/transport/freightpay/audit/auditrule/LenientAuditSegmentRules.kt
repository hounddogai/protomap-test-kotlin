package com.acme.transport.freightpay.audit.auditrule

/** A rule a lenient audit segment must pass. Check returns its problems, or none. */
interface LenientAuditSegmentRule {
    fun check(item: LenientAuditSegment): MutableList<String>
}

data class LenientAuditSegmentRateRangeRule(var min: Double, var max: Double) : LenientAuditSegmentRule {
    override fun check(item: LenientAuditSegment): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class LenientAuditSegmentRequiredTimezoneRule(var message: String) : LenientAuditSegmentRule {
    override fun check(item: LenientAuditSegment): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientAuditSegmentLabelLengthRule(var maxLength: Int) : LenientAuditSegmentRule {
    override fun check(item: LenientAuditSegment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient audit segment. */
data class LenientAuditSegmentValidator(var rules: MutableList<LenientAuditSegmentRule>) {
    fun validate(item: LenientAuditSegment): MutableList<String> {
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

    fun requireValid(item: LenientAuditSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientAuditSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientAuditSegmentValidator(): LenientAuditSegmentValidator {
    return LenientAuditSegmentValidator(
        mutableListOf<LenientAuditSegmentRule>(
            LenientAuditSegmentRateRangeRule(4.7, 2297.2),
            LenientAuditSegmentRequiredTimezoneRule("lenient audit segment needs this field"),
            LenientAuditSegmentLabelLengthRule(32),
        ),
    )
}
