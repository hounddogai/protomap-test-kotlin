package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.kit.text.normalize

/** A rule a fuel surcharge review must pass. Check returns its problems, or none. */
interface FuelSurchargeReviewRule {
    fun check(item: FuelSurchargeReview): MutableList<String>
}

data class FuelSurchargeReviewCountryLengthRule(var maxLength: Int) : FuelSurchargeReviewRule {
    override fun check(item: FuelSurchargeReview): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeReviewAllowedDriverIdRule(var allowed: MutableList<String>) : FuelSurchargeReviewRule {
    override fun check(item: FuelSurchargeReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is missing")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeReviewRequiredDriverIdRule(var message: String) : FuelSurchargeReviewRule {
    override fun check(item: FuelSurchargeReview): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeReviewRequiredLabelRule(var message: String) : FuelSurchargeReviewRule {
    override fun check(item: FuelSurchargeReview): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeReviewCodeLengthRule(var maxLength: Int) : FuelSurchargeReviewRule {
    override fun check(item: FuelSurchargeReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge review. */
data class FuelSurchargeReviewValidator(var rules: MutableList<FuelSurchargeReviewRule>) {
    fun validate(item: FuelSurchargeReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelSurchargeReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeReview>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeReviewValidator(): FuelSurchargeReviewValidator {
    return FuelSurchargeReviewValidator(
        mutableListOf<FuelSurchargeReviewRule>(
            FuelSurchargeReviewCountryLengthRule(80),
            FuelSurchargeReviewAllowedDriverIdRule(mutableListOf<String>("manual", "scheduled", "pending")),
            FuelSurchargeReviewRequiredDriverIdRule("fuel surcharge review needs an entry"),
            FuelSurchargeReviewRequiredLabelRule("fuel surcharge review needs a value"),
            FuelSurchargeReviewCodeLengthRule(86400),
        ),
    )
}
