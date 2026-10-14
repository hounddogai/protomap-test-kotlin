package com.acme.transport.emissions.idling.allocation

/** A rule a seasonal idling claim must pass. Check returns its problems, or none. */
interface SeasonalIdlingClaimRule {
    fun check(item: SeasonalIdlingClaim): MutableList<String>
}

data class SeasonalIdlingClaimCodeLengthRule(var maxLength: Int) : SeasonalIdlingClaimRule {
    override fun check(item: SeasonalIdlingClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalIdlingClaimLabelLengthRule(var maxLength: Int) : SeasonalIdlingClaimRule {
    override fun check(item: SeasonalIdlingClaim): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalIdlingClaimRequiredCodeRule(var message: String) : SeasonalIdlingClaimRule {
    override fun check(item: SeasonalIdlingClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal idling claim. */
data class SeasonalIdlingClaimValidator(var rules: MutableList<SeasonalIdlingClaimRule>) {
    fun validate(item: SeasonalIdlingClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalIdlingClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalIdlingClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalIdlingClaimValidator(): SeasonalIdlingClaimValidator {
    return SeasonalIdlingClaimValidator(
        mutableListOf<SeasonalIdlingClaimRule>(
            SeasonalIdlingClaimCodeLengthRule(120),
            SeasonalIdlingClaimLabelLengthRule(16),
            SeasonalIdlingClaimRequiredCodeRule("seasonal idling claim needs a value"),
        ),
    )
}
