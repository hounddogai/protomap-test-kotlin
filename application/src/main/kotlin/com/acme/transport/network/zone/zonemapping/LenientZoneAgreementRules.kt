package com.acme.transport.network.zone.zonemapping

/** A rule a lenient zone agreement must pass. Check returns its problems, or none. */
interface LenientZoneAgreementRule {
    fun check(item: LenientZoneAgreement): MutableList<String>
}

data class LenientZoneAgreementRequiredOwnerIdRule(var message: String) : LenientZoneAgreementRule {
    override fun check(item: LenientZoneAgreement): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientZoneAgreementCodePrefixRule(var prefix: String) : LenientZoneAgreementRule {
    override fun check(item: LenientZoneAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientZoneAgreementOdometerRangeRule(var min: Int, var max: Int) : LenientZoneAgreementRule {
    override fun check(item: LenientZoneAgreement): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientZoneAgreementOwnerIdLengthRule(var maxLength: Int) : LenientZoneAgreementRule {
    override fun check(item: LenientZoneAgreement): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient zone agreement. */
data class LenientZoneAgreementValidator(var rules: MutableList<LenientZoneAgreementRule>) {
    fun validate(item: LenientZoneAgreement): MutableList<String> {
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

    fun requireValid(item: LenientZoneAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientZoneAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientZoneAgreementValidator(): LenientZoneAgreementValidator {
    return LenientZoneAgreementValidator(
        mutableListOf<LenientZoneAgreementRule>(
            LenientZoneAgreementRequiredOwnerIdRule("lenient zone agreement needs an entry"),
            LenientZoneAgreementCodePrefixRule("ACM"),
            LenientZoneAgreementOdometerRangeRule(32, 255),
            LenientZoneAgreementOwnerIdLengthRule(86400),
        ),
    )
}
