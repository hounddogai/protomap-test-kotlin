package com.acme.transport.lastmile.locker.agreement

/** A rule a legacy locker agreement must pass. Check returns its problems, or none. */
interface LegacyLockerAgreementRule {
    fun check(item: LegacyLockerAgreement): MutableList<String>
}

data class LegacyLockerAgreementPalletsRangeRule(var min: Int, var max: Int) : LegacyLockerAgreementRule {
    override fun check(item: LegacyLockerAgreement): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLockerAgreementCodeLengthRule(var maxLength: Int) : LegacyLockerAgreementRule {
    override fun check(item: LegacyLockerAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLockerAgreementCodePrefixRule(var prefix: String) : LegacyLockerAgreementRule {
    override fun check(item: LegacyLockerAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLockerAgreementDescriptionLengthRule(var maxLength: Int) : LegacyLockerAgreementRule {
    override fun check(item: LegacyLockerAgreement): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy locker agreement. */
data class LegacyLockerAgreementValidator(var rules: MutableList<LegacyLockerAgreementRule>) {
    fun validate(item: LegacyLockerAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyLockerAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyLockerAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyLockerAgreementValidator(): LegacyLockerAgreementValidator {
    return LegacyLockerAgreementValidator(
        mutableListOf<LegacyLockerAgreementRule>(
            LegacyLockerAgreementPalletsRangeRule(32, 3600),
            LegacyLockerAgreementCodeLengthRule(3600),
            LegacyLockerAgreementCodePrefixRule("ACM"),
            LegacyLockerAgreementDescriptionLengthRule(86400),
        ),
    )
}
