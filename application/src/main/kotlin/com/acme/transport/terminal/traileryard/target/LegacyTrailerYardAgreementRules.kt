package com.acme.transport.terminal.traileryard.target

/** A rule a legacy trailer yard agreement must pass. Check returns its problems, or none. */
interface LegacyTrailerYardAgreementRule {
    fun check(item: LegacyTrailerYardAgreement): MutableList<String>
}

data class LegacyTrailerYardAgreementOdometerRangeRule(
    var min: Int,
    var max: Int,
) : LegacyTrailerYardAgreementRule {
    override fun check(item: LegacyTrailerYardAgreement): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTrailerYardAgreementVersionRangeRule(
    var min: Int,
    var max: Int,
) : LegacyTrailerYardAgreementRule {
    override fun check(item: LegacyTrailerYardAgreement): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class LegacyTrailerYardAgreementCurrencyLengthRule(var maxLength: Int) : LegacyTrailerYardAgreementRule {
    override fun check(item: LegacyTrailerYardAgreement): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTrailerYardAgreementRequiredCurrencyRule(
    var message: String,
) : LegacyTrailerYardAgreementRule {
    override fun check(item: LegacyTrailerYardAgreement): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyTrailerYardAgreementRequiredDepotRule(var message: String) : LegacyTrailerYardAgreementRule {
    override fun check(item: LegacyTrailerYardAgreement): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy trailer yard agreement. */
data class LegacyTrailerYardAgreementValidator(var rules: MutableList<LegacyTrailerYardAgreementRule>) {
    fun validate(item: LegacyTrailerYardAgreement): MutableList<String> {
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

    fun requireValid(item: LegacyTrailerYardAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTrailerYardAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTrailerYardAgreementValidator(): LegacyTrailerYardAgreementValidator {
    return LegacyTrailerYardAgreementValidator(
        mutableListOf<LegacyTrailerYardAgreementRule>(
            LegacyTrailerYardAgreementOdometerRangeRule(1000, 86400),
            LegacyTrailerYardAgreementVersionRangeRule(1000, 86400),
            LegacyTrailerYardAgreementCurrencyLengthRule(64),
            LegacyTrailerYardAgreementRequiredCurrencyRule("legacy trailer yard agreement needs this field"),
            LegacyTrailerYardAgreementRequiredDepotRule("legacy trailer yard agreement needs a value"),
        ),
    )
}
