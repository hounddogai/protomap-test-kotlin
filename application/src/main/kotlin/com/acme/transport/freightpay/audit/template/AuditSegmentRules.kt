package com.acme.transport.freightpay.audit.template

/** A rule a audit segment must pass. Check returns its problems, or none. */
interface AuditSegmentRule {
    fun check(item: AuditSegment): MutableList<String>
}

data class AuditSegmentDescriptionLengthRule(var maxLength: Int) : AuditSegmentRule {
    override fun check(item: AuditSegment): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditSegmentRequiredCodeRule(var message: String) : AuditSegmentRule {
    override fun check(item: AuditSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditSegmentCodeLengthRule(var maxLength: Int) : AuditSegmentRule {
    override fun check(item: AuditSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditSegmentCodePrefixRule(var prefix: String) : AuditSegmentRule {
    override fun check(item: AuditSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AuditSegmentRequiredDescriptionRule(var message: String) : AuditSegmentRule {
    override fun check(item: AuditSegment): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditSegmentTitleLengthRule(var maxLength: Int) : AuditSegmentRule {
    override fun check(item: AuditSegment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit segment. */
data class AuditSegmentValidator(var rules: MutableList<AuditSegmentRule>) {
    fun validate(item: AuditSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditSegmentValidator(): AuditSegmentValidator {
    return AuditSegmentValidator(
        mutableListOf<AuditSegmentRule>(
            AuditSegmentDescriptionLengthRule(80),
            AuditSegmentRequiredCodeRule("audit segment needs a value"),
            AuditSegmentCodeLengthRule(3600),
            AuditSegmentCodePrefixRule("X"),
            AuditSegmentRequiredDescriptionRule("audit segment needs this field"),
            AuditSegmentTitleLengthRule(255),
        ),
    )
}
