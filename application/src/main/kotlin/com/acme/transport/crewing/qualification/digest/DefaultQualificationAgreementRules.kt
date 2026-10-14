package com.acme.transport.crewing.qualification.digest

/** A rule a default qualification agreement must pass. Check returns its problems, or none. */
interface DefaultQualificationAgreementRule {
    fun check(item: DefaultQualificationAgreement): MutableList<String>
}

data class DefaultQualificationAgreementCodePrefixRule(var prefix: String) : DefaultQualificationAgreementRule {
    override fun check(item: DefaultQualificationAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultQualificationAgreementRequiredTitleRule(
    var message: String,
) : DefaultQualificationAgreementRule {
    override fun check(item: DefaultQualificationAgreement): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultQualificationAgreementCategoryLengthRule(
    var maxLength: Int,
) : DefaultQualificationAgreementRule {
    override fun check(item: DefaultQualificationAgreement): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultQualificationAgreementDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : DefaultQualificationAgreementRule {
    override fun check(item: DefaultQualificationAgreement): MutableList<String> {
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

/** Runs every rule of a default qualification agreement. */
data class DefaultQualificationAgreementValidator(var rules: MutableList<DefaultQualificationAgreementRule>) {
    fun validate(item: DefaultQualificationAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultQualificationAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultQualificationAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultQualificationAgreementValidator(): DefaultQualificationAgreementValidator {
    return DefaultQualificationAgreementValidator(
        mutableListOf<DefaultQualificationAgreementRule>(
            DefaultQualificationAgreementCodePrefixRule("ACM"),
            DefaultQualificationAgreementRequiredTitleRule("default qualification agreement needs this field"),
            DefaultQualificationAgreementCategoryLengthRule(255),
            DefaultQualificationAgreementDistanceKmRangeRule(2.1, 1591.1),
        ),
    )
}
