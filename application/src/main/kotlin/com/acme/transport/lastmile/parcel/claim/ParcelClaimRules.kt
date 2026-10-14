package com.acme.transport.lastmile.parcel.claim

/** A rule a parcel claim must pass. Check returns its problems, or none. */
interface ParcelClaimRule {
    fun check(item: ParcelClaim): MutableList<String>
}

data class ParcelClaimReviewerLengthRule(var maxLength: Int) : ParcelClaimRule {
    override fun check(item: ParcelClaim): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelClaimRequiredReviewerRule(var message: String) : ParcelClaimRule {
    override fun check(item: ParcelClaim): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelClaimRequiredCodeRule(var message: String) : ParcelClaimRule {
    override fun check(item: ParcelClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelClaimOdometerRangeRule(var min: Int, var max: Int) : ParcelClaimRule {
    override fun check(item: ParcelClaim): MutableList<String> {
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

data class ParcelClaimWindowDaysRangeRule(var min: Int, var max: Int) : ParcelClaimRule {
    override fun check(item: ParcelClaim): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ParcelClaimSequenceRangeRule(var min: Int, var max: Int) : ParcelClaimRule {
    override fun check(item: ParcelClaim): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a parcel claim. */
data class ParcelClaimValidator(var rules: MutableList<ParcelClaimRule>) {
    fun validate(item: ParcelClaim): MutableList<String> {
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

    fun requireValid(item: ParcelClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelClaimValidator(): ParcelClaimValidator {
    return ParcelClaimValidator(
        mutableListOf<ParcelClaimRule>(
            ParcelClaimReviewerLengthRule(64),
            ParcelClaimRequiredReviewerRule("parcel claim needs an entry"),
            ParcelClaimRequiredCodeRule("parcel claim needs this field"),
            ParcelClaimOdometerRangeRule(32, 32),
            ParcelClaimWindowDaysRangeRule(32, 64),
            ParcelClaimSequenceRangeRule(3600, 3600),
        ),
    )
}
