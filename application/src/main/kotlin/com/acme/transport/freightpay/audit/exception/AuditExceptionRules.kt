package com.acme.transport.freightpay.audit.exception

import com.acme.transport.kit.text.normalize

/** A rule a audit exception must pass. Check returns its problems, or none. */
interface AuditExceptionRule {
    fun check(item: AuditException): MutableList<String>
}

data class AuditExceptionNameLengthRule(var maxLength: Int) : AuditExceptionRule {
    override fun check(item: AuditException): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditExceptionContactEmailLengthRule(var maxLength: Int) : AuditExceptionRule {
    override fun check(item: AuditException): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditExceptionFuelLitersRangeRule(var min: Double, var max: Double) : AuditExceptionRule {
    override fun check(item: AuditException): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class AuditExceptionCodeLengthRule(var maxLength: Int) : AuditExceptionRule {
    override fun check(item: AuditException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditExceptionRequiredContactEmailRule(var message: String) : AuditExceptionRule {
    override fun check(item: AuditException): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditExceptionAllowedContactEmailRule(var allowed: MutableList<String>) : AuditExceptionRule {
    override fun check(item: AuditException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit exception. */
data class AuditExceptionValidator(var rules: MutableList<AuditExceptionRule>) {
    fun validate(item: AuditException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditException>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditExceptionValidator(): AuditExceptionValidator {
    return AuditExceptionValidator(
        mutableListOf<AuditExceptionRule>(
            AuditExceptionNameLengthRule(120),
            AuditExceptionContactEmailLengthRule(80),
            AuditExceptionFuelLitersRangeRule(3.1, 244.2),
            AuditExceptionCodeLengthRule(3600),
            AuditExceptionRequiredContactEmailRule("audit exception needs this field"),
            AuditExceptionAllowedContactEmailRule(mutableListOf<String>("adaptive")),
        ),
    )
}
