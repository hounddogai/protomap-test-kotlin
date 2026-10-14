package com.acme.transport.linehaul.trailer.claim

/** A rule a trailer agreement must pass. Check returns its problems, or none. */
interface TrailerAgreementRule {
    fun check(item: TrailerAgreement): MutableList<String>
}

data class TrailerAgreementRequiredCategoryRule(var message: String) : TrailerAgreementRule {
    override fun check(item: TrailerAgreement): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerAgreementLoadKgRangeRule(var min: Double, var max: Double) : TrailerAgreementRule {
    override fun check(item: TrailerAgreement): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class TrailerAgreementCodePrefixRule(var prefix: String) : TrailerAgreementRule {
    override fun check(item: TrailerAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer agreement. */
data class TrailerAgreementValidator(var rules: MutableList<TrailerAgreementRule>) {
    fun validate(item: TrailerAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerAgreementValidator(): TrailerAgreementValidator {
    return TrailerAgreementValidator(
        mutableListOf<TrailerAgreementRule>(
            TrailerAgreementRequiredCategoryRule("trailer agreement needs a value"),
            TrailerAgreementLoadKgRangeRule(7.9, 4210.4),
            TrailerAgreementCodePrefixRule("OPS"),
        ),
    )
}
