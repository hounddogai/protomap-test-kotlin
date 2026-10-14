package com.acme.transport.linehaul.tender.batch

/** A rule a tender claim must pass. Check returns its problems, or none. */
interface TenderClaimRule {
    fun check(item: TenderClaim): MutableList<String>
}

data class TenderClaimLaneLengthRule(var maxLength: Int) : TenderClaimRule {
    override fun check(item: TenderClaim): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderClaimCapacityRangeRule(var min: Int, var max: Int) : TenderClaimRule {
    override fun check(item: TenderClaim): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class TenderClaimRequiredCodeRule(var message: String) : TenderClaimRule {
    override fun check(item: TenderClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender claim. */
data class TenderClaimValidator(var rules: MutableList<TenderClaimRule>) {
    fun validate(item: TenderClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TenderClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderClaimValidator(): TenderClaimValidator {
    return TenderClaimValidator(
        mutableListOf<TenderClaimRule>(
            TenderClaimLaneLengthRule(120),
            TenderClaimCapacityRangeRule(16, 32),
            TenderClaimRequiredCodeRule("tender claim needs an entry"),
        ),
    )
}
