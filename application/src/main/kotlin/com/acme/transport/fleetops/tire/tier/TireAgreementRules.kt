package com.acme.transport.fleetops.tire.tier

/** A rule a tire agreement must pass. Check returns its problems, or none. */
interface TireAgreementRule {
    fun check(item: TireAgreement): MutableList<String>
}

data class TireAgreementTitleLengthRule(var maxLength: Int) : TireAgreementRule {
    override fun check(item: TireAgreement): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireAgreementStartsAtRangeRule(var min: Int, var max: Int) : TireAgreementRule {
    override fun check(item: TireAgreement): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TireAgreementDistanceKmRangeRule(var min: Double, var max: Double) : TireAgreementRule {
    override fun check(item: TireAgreement): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tire agreement. */
data class TireAgreementValidator(var rules: MutableList<TireAgreementRule>) {
    fun validate(item: TireAgreement): MutableList<String> {
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

    fun requireValid(item: TireAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultTireAgreementValidator(): TireAgreementValidator {
    return TireAgreementValidator(
        mutableListOf<TireAgreementRule>(
            TireAgreementTitleLengthRule(120),
            TireAgreementStartsAtRangeRule(32, 120),
            TireAgreementDistanceKmRangeRule(8.7, 1491.8),
        ),
    )
}
