package com.acme.transport.lastmile.locker.tier

/** A rule a tiered locker agreement must pass. Check returns its problems, or none. */
interface TieredLockerAgreementRule {
    fun check(item: TieredLockerAgreement): MutableList<String>
}

data class TieredLockerAgreementCodeLengthRule(var maxLength: Int) : TieredLockerAgreementRule {
    override fun check(item: TieredLockerAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredLockerAgreementCreatedAtRangeRule(var min: Int, var max: Int) : TieredLockerAgreementRule {
    override fun check(item: TieredLockerAgreement): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredLockerAgreementOdometerRangeRule(var min: Int, var max: Int) : TieredLockerAgreementRule {
    override fun check(item: TieredLockerAgreement): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tiered locker agreement. */
data class TieredLockerAgreementValidator(var rules: MutableList<TieredLockerAgreementRule>) {
    fun validate(item: TieredLockerAgreement): MutableList<String> {
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

    fun requireValid(item: TieredLockerAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredLockerAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredLockerAgreementValidator(): TieredLockerAgreementValidator {
    return TieredLockerAgreementValidator(
        mutableListOf<TieredLockerAgreementRule>(
            TieredLockerAgreementCodeLengthRule(64),
            TieredLockerAgreementCreatedAtRangeRule(16, 86400),
            TieredLockerAgreementOdometerRangeRule(32, 3600),
        ),
    )
}
