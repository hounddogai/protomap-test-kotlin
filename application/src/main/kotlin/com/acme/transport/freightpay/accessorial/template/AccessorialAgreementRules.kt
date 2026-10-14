package com.acme.transport.freightpay.accessorial.template

/** A rule a accessorial agreement must pass. Check returns its problems, or none. */
interface AccessorialAgreementRule {
    fun check(item: AccessorialAgreement): MutableList<String>
}

data class AccessorialAgreementNameLengthRule(var maxLength: Int) : AccessorialAgreementRule {
    override fun check(item: AccessorialAgreement): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialAgreementDockLengthRule(var maxLength: Int) : AccessorialAgreementRule {
    override fun check(item: AccessorialAgreement): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialAgreementCodePrefixRule(var prefix: String) : AccessorialAgreementRule {
    override fun check(item: AccessorialAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial agreement. */
data class AccessorialAgreementValidator(var rules: MutableList<AccessorialAgreementRule>) {
    fun validate(item: AccessorialAgreement): MutableList<String> {
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

    fun requireValid(item: AccessorialAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialAgreementValidator(): AccessorialAgreementValidator {
    return AccessorialAgreementValidator(
        mutableListOf<AccessorialAgreementRule>(
            AccessorialAgreementNameLengthRule(86400),
            AccessorialAgreementDockLengthRule(1000),
            AccessorialAgreementCodePrefixRule("X"),
        ),
    )
}
