package com.acme.transport.safety.nearmiss.missclaim

/** A rule a adaptive near miss claim must pass. Check returns its problems, or none. */
interface AdaptiveNearMissClaimRule {
    fun check(item: AdaptiveNearMissClaim): MutableList<String>
}

data class AdaptiveNearMissClaimExternalRefLengthRule(var maxLength: Int) : AdaptiveNearMissClaimRule {
    override fun check(item: AdaptiveNearMissClaim): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNearMissClaimDockLengthRule(var maxLength: Int) : AdaptiveNearMissClaimRule {
    override fun check(item: AdaptiveNearMissClaim): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNearMissClaimRequiredCodeRule(var message: String) : AdaptiveNearMissClaimRule {
    override fun check(item: AdaptiveNearMissClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive near miss claim. */
data class AdaptiveNearMissClaimValidator(var rules: MutableList<AdaptiveNearMissClaimRule>) {
    fun validate(item: AdaptiveNearMissClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveNearMissClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveNearMissClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveNearMissClaimValidator(): AdaptiveNearMissClaimValidator {
    return AdaptiveNearMissClaimValidator(
        mutableListOf<AdaptiveNearMissClaimRule>(
            AdaptiveNearMissClaimExternalRefLengthRule(32),
            AdaptiveNearMissClaimDockLengthRule(120),
            AdaptiveNearMissClaimRequiredCodeRule("adaptive near miss claim needs an entry"),
        ),
    )
}
