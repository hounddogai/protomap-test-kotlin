package com.acme.transport.terminal.traileryard.estimate

/** A rule a trailer yard agreement must pass. Check returns its problems, or none. */
interface TrailerYardAgreementRule {
    fun check(item: TrailerYardAgreement): MutableList<String>
}

data class TrailerYardAgreementCodePrefixRule(var prefix: String) : TrailerYardAgreementRule {
    override fun check(item: TrailerYardAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardAgreementRatioRangeRule(var min: Double, var max: Double) : TrailerYardAgreementRule {
    override fun check(item: TrailerYardAgreement): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardAgreementTitleLengthRule(var maxLength: Int) : TrailerYardAgreementRule {
    override fun check(item: TrailerYardAgreement): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard agreement. */
data class TrailerYardAgreementValidator(var rules: MutableList<TrailerYardAgreementRule>) {
    fun validate(item: TrailerYardAgreement): MutableList<String> {
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

    fun requireValid(item: TrailerYardAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardAgreementValidator(): TrailerYardAgreementValidator {
    return TrailerYardAgreementValidator(
        mutableListOf<TrailerYardAgreementRule>(
            TrailerYardAgreementCodePrefixRule("ACM"),
            TrailerYardAgreementRatioRangeRule(9.1, 2349.2),
            TrailerYardAgreementTitleLengthRule(1000),
        ),
    )
}
