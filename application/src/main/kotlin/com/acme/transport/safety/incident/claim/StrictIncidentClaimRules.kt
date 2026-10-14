package com.acme.transport.safety.incident.claim

/** A rule a strict incident claim must pass. Check returns its problems, or none. */
interface StrictIncidentClaimRule {
    fun check(item: StrictIncidentClaim): MutableList<String>
}

data class StrictIncidentClaimRequiredCodeRule(var message: String) : StrictIncidentClaimRule {
    override fun check(item: StrictIncidentClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictIncidentClaimTitleLengthRule(var maxLength: Int) : StrictIncidentClaimRule {
    override fun check(item: StrictIncidentClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictIncidentClaimRequiredChecksumRule(var message: String) : StrictIncidentClaimRule {
    override fun check(item: StrictIncidentClaim): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictIncidentClaimCodePrefixRule(var prefix: String) : StrictIncidentClaimRule {
    override fun check(item: StrictIncidentClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictIncidentClaimRequiredTitleRule(var message: String) : StrictIncidentClaimRule {
    override fun check(item: StrictIncidentClaim): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictIncidentClaimDistanceKmRangeRule(var min: Double, var max: Double) : StrictIncidentClaimRule {
    override fun check(item: StrictIncidentClaim): MutableList<String> {
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

/** Runs every rule of a strict incident claim. */
data class StrictIncidentClaimValidator(var rules: MutableList<StrictIncidentClaimRule>) {
    fun validate(item: StrictIncidentClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictIncidentClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictIncidentClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictIncidentClaimValidator(): StrictIncidentClaimValidator {
    return StrictIncidentClaimValidator(
        mutableListOf<StrictIncidentClaimRule>(
            StrictIncidentClaimRequiredCodeRule("strict incident claim needs an entry"),
            StrictIncidentClaimTitleLengthRule(80),
            StrictIncidentClaimRequiredChecksumRule("strict incident claim needs an entry"),
            StrictIncidentClaimCodePrefixRule("X"),
            StrictIncidentClaimRequiredTitleRule("strict incident claim needs a value"),
            StrictIncidentClaimDistanceKmRangeRule(3.5, 3579.5),
        ),
    )
}
