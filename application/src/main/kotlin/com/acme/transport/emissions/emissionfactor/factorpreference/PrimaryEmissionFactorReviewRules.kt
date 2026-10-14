package com.acme.transport.emissions.emissionfactor.factorpreference

/** A rule a primary emission factor review must pass. Check returns its problems, or none. */
interface PrimaryEmissionFactorReviewRule {
    fun check(item: PrimaryEmissionFactorReview): MutableList<String>
}

data class PrimaryEmissionFactorReviewCapacityRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryEmissionFactorReviewRule {
    override fun check(item: PrimaryEmissionFactorReview): MutableList<String> {
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

data class PrimaryEmissionFactorReviewContactEmailLengthRule(
    var maxLength: Int,
) : PrimaryEmissionFactorReviewRule {
    override fun check(item: PrimaryEmissionFactorReview): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryEmissionFactorReviewLabelLengthRule(var maxLength: Int) : PrimaryEmissionFactorReviewRule {
    override fun check(item: PrimaryEmissionFactorReview): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryEmissionFactorReviewRequiredLabelRule(var message: String) : PrimaryEmissionFactorReviewRule {
    override fun check(item: PrimaryEmissionFactorReview): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary emission factor review. */
data class PrimaryEmissionFactorReviewValidator(var rules: MutableList<PrimaryEmissionFactorReviewRule>) {
    fun validate(item: PrimaryEmissionFactorReview): MutableList<String> {
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

    fun requireValid(item: PrimaryEmissionFactorReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryEmissionFactorReview>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryEmissionFactorReviewValidator(): PrimaryEmissionFactorReviewValidator {
    return PrimaryEmissionFactorReviewValidator(
        mutableListOf<PrimaryEmissionFactorReviewRule>(
            PrimaryEmissionFactorReviewCapacityRangeRule(255, 86400),
            PrimaryEmissionFactorReviewContactEmailLengthRule(255),
            PrimaryEmissionFactorReviewLabelLengthRule(120),
            PrimaryEmissionFactorReviewRequiredLabelRule("primary emission factor review needs an entry"),
        ),
    )
}
