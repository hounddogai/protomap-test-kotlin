package com.acme.transport.freightpay.accessorial.accessorialcycle

/** A rule a accessorial ledger must pass. Check returns its problems, or none. */
interface AccessorialLedgerRule {
    fun check(item: AccessorialLedger): MutableList<String>
}

data class AccessorialLedgerCodePrefixRule(var prefix: String) : AccessorialLedgerRule {
    override fun check(item: AccessorialLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialLedgerDriverIdLengthRule(var maxLength: Int) : AccessorialLedgerRule {
    override fun check(item: AccessorialLedger): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialLedgerCodeLengthRule(var maxLength: Int) : AccessorialLedgerRule {
    override fun check(item: AccessorialLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialLedgerCreatedAtRangeRule(var min: Int, var max: Int) : AccessorialLedgerRule {
    override fun check(item: AccessorialLedger): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialLedgerRequiredCodeRule(var message: String) : AccessorialLedgerRule {
    override fun check(item: AccessorialLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialLedgerSourceLengthRule(var maxLength: Int) : AccessorialLedgerRule {
    override fun check(item: AccessorialLedger): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial ledger. */
data class AccessorialLedgerValidator(var rules: MutableList<AccessorialLedgerRule>) {
    fun validate(item: AccessorialLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AccessorialLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialLedgerValidator(): AccessorialLedgerValidator {
    return AccessorialLedgerValidator(
        mutableListOf<AccessorialLedgerRule>(
            AccessorialLedgerCodePrefixRule("Q"),
            AccessorialLedgerDriverIdLengthRule(80),
            AccessorialLedgerCodeLengthRule(255),
            AccessorialLedgerCreatedAtRangeRule(255, 86400),
            AccessorialLedgerRequiredCodeRule("accessorial ledger needs this field"),
            AccessorialLedgerSourceLengthRule(1000),
        ),
    )
}
