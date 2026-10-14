package com.acme.transport.safety.incident.segment

/** A rule a incident claim must pass. Check returns its problems, or none. */
interface IncidentClaimRule {
    fun check(item: IncidentClaim): MutableList<String>
}

data class IncidentClaimCodePrefixRule(var prefix: String) : IncidentClaimRule {
    override fun check(item: IncidentClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class IncidentClaimRequiredLabelRule(var message: String) : IncidentClaimRule {
    override fun check(item: IncidentClaim): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentClaimWeightRangeRule(var min: Double, var max: Double) : IncidentClaimRule {
    override fun check(item: IncidentClaim): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IncidentClaimLoadKgRangeRule(var min: Double, var max: Double) : IncidentClaimRule {
    override fun check(item: IncidentClaim): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IncidentClaimRetriesRangeRule(var min: Int, var max: Int) : IncidentClaimRule {
    override fun check(item: IncidentClaim): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident claim. */
data class IncidentClaimValidator(var rules: MutableList<IncidentClaimRule>) {
    fun validate(item: IncidentClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IncidentClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentClaimValidator(): IncidentClaimValidator {
    return IncidentClaimValidator(
        mutableListOf<IncidentClaimRule>(
            IncidentClaimCodePrefixRule("OPS"),
            IncidentClaimRequiredLabelRule("incident claim needs an entry"),
            IncidentClaimWeightRangeRule(9.7, 708.4),
            IncidentClaimLoadKgRangeRule(5.1, 1634.9),
            IncidentClaimRetriesRangeRule(64, 80),
        ),
    )
}
