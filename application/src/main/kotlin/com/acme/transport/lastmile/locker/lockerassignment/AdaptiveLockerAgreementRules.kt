package com.acme.transport.lastmile.locker.lockerassignment

import com.acme.transport.kit.text.normalize

/** A rule a adaptive locker agreement must pass. Check returns its problems, or none. */
interface AdaptiveLockerAgreementRule {
    fun check(item: AdaptiveLockerAgreement): MutableList<String>
}

data class AdaptiveLockerAgreementRequiredNameRule(var message: String) : AdaptiveLockerAgreementRule {
    override fun check(item: AdaptiveLockerAgreement): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLockerAgreementAllowedNameRule(
    var allowed: MutableList<String>,
) : AdaptiveLockerAgreementRule {
    override fun check(item: AdaptiveLockerAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLockerAgreementChecksumLengthRule(var maxLength: Int) : AdaptiveLockerAgreementRule {
    override fun check(item: AdaptiveLockerAgreement): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLockerAgreementCodeLengthRule(var maxLength: Int) : AdaptiveLockerAgreementRule {
    override fun check(item: AdaptiveLockerAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLockerAgreementNameLengthRule(var maxLength: Int) : AdaptiveLockerAgreementRule {
    override fun check(item: AdaptiveLockerAgreement): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLockerAgreementDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveLockerAgreementRule {
    override fun check(item: AdaptiveLockerAgreement): MutableList<String> {
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

/** Runs every rule of a adaptive locker agreement. */
data class AdaptiveLockerAgreementValidator(var rules: MutableList<AdaptiveLockerAgreementRule>) {
    fun validate(item: AdaptiveLockerAgreement): MutableList<String> {
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

    fun requireValid(item: AdaptiveLockerAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveLockerAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveLockerAgreementValidator(): AdaptiveLockerAgreementValidator {
    return AdaptiveLockerAgreementValidator(
        mutableListOf<AdaptiveLockerAgreementRule>(
            AdaptiveLockerAgreementRequiredNameRule("adaptive locker agreement needs an entry"),
            AdaptiveLockerAgreementAllowedNameRule(mutableListOf<String>("rolling", "adaptive")),
            AdaptiveLockerAgreementChecksumLengthRule(255),
            AdaptiveLockerAgreementCodeLengthRule(80),
            AdaptiveLockerAgreementNameLengthRule(1000),
            AdaptiveLockerAgreementDistanceKmRangeRule(3.5, 4854.9),
        ),
    )
}
