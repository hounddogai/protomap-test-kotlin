package com.acme.transport.lastmile.parcel.variance

import com.acme.transport.kit.text.normalize

/** A rule a parcel variance must pass. Check returns its problems, or none. */
interface ParcelVarianceRule {
    fun check(item: ParcelVariance): MutableList<String>
}

data class ParcelVariancePhoneLengthRule(var maxLength: Int) : ParcelVarianceRule {
    override fun check(item: ParcelVariance): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelVarianceAmountRangeRule(var min: Double, var max: Double) : ParcelVarianceRule {
    override fun check(item: ParcelVariance): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ParcelVarianceAllowedChecksumRule(var allowed: MutableList<String>) : ParcelVarianceRule {
    override fun check(item: ParcelVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class ParcelVarianceRequiredChecksumRule(var message: String) : ParcelVarianceRule {
    override fun check(item: ParcelVariance): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelVarianceRequiredPhoneRule(var message: String) : ParcelVarianceRule {
    override fun check(item: ParcelVariance): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelVarianceChecksumLengthRule(var maxLength: Int) : ParcelVarianceRule {
    override fun check(item: ParcelVariance): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel variance. */
data class ParcelVarianceValidator(var rules: MutableList<ParcelVarianceRule>) {
    fun validate(item: ParcelVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ParcelVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelVarianceValidator(): ParcelVarianceValidator {
    return ParcelVarianceValidator(
        mutableListOf<ParcelVarianceRule>(
            ParcelVariancePhoneLengthRule(16),
            ParcelVarianceAmountRangeRule(7.4, 2136.0),
            ParcelVarianceAllowedChecksumRule(mutableListOf<String>("rolling")),
            ParcelVarianceRequiredChecksumRule("parcel variance needs this field"),
            ParcelVarianceRequiredPhoneRule("parcel variance needs this field"),
            ParcelVarianceChecksumLengthRule(16),
        ),
    )
}
