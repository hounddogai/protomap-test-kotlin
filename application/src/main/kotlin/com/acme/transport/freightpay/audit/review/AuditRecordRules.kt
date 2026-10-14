package com.acme.transport.freightpay.audit.review

/** A rule a audit record must pass. Check returns its problems, or none. */
interface AuditRecordRule {
    fun check(item: AuditRecord): MutableList<String>
}

data class AuditRecordRequiredCodeRule(var message: String) : AuditRecordRule {
    override fun check(item: AuditRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditRecordCodeLengthRule(var maxLength: Int) : AuditRecordRule {
    override fun check(item: AuditRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditRecordWindowDaysRangeRule(var min: Int, var max: Int) : AuditRecordRule {
    override fun check(item: AuditRecord): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AuditRecordLabelLengthRule(var maxLength: Int) : AuditRecordRule {
    override fun check(item: AuditRecord): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditRecordGraceDaysRangeRule(var min: Int, var max: Int) : AuditRecordRule {
    override fun check(item: AuditRecord): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit record. */
data class AuditRecordValidator(var rules: MutableList<AuditRecordRule>) {
    fun validate(item: AuditRecord): MutableList<String> {
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

    fun requireValid(item: AuditRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditRecordValidator(): AuditRecordValidator {
    return AuditRecordValidator(
        mutableListOf<AuditRecordRule>(
            AuditRecordRequiredCodeRule("audit record needs an entry"),
            AuditRecordCodeLengthRule(86400),
            AuditRecordWindowDaysRangeRule(120, 3600),
            AuditRecordLabelLengthRule(80),
            AuditRecordGraceDaysRangeRule(86400, 86400),
        ),
    )
}
