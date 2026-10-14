package com.acme.transport.safety.incident.request

/** A rule a fallback incident request must pass. Check returns its problems, or none. */
interface FallbackIncidentRequestRule {
    fun check(item: FallbackIncidentRequest): MutableList<String>
}

data class FallbackIncidentRequestCodeLengthRule(var maxLength: Int) : FallbackIncidentRequestRule {
    override fun check(item: FallbackIncidentRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackIncidentRequestRequiredOwnerIdRule(var message: String) : FallbackIncidentRequestRule {
    override fun check(item: FallbackIncidentRequest): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackIncidentRequestRequiredCodeRule(var message: String) : FallbackIncidentRequestRule {
    override fun check(item: FallbackIncidentRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackIncidentRequestEndsAtRangeRule(var min: Int, var max: Int) : FallbackIncidentRequestRule {
    override fun check(item: FallbackIncidentRequest): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class FallbackIncidentRequestRequiredLabelRule(var message: String) : FallbackIncidentRequestRule {
    override fun check(item: FallbackIncidentRequest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackIncidentRequestWeightRangeRule(
    var min: Double,
    var max: Double,
) : FallbackIncidentRequestRule {
    override fun check(item: FallbackIncidentRequest): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback incident request. */
data class FallbackIncidentRequestValidator(var rules: MutableList<FallbackIncidentRequestRule>) {
    fun validate(item: FallbackIncidentRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackIncidentRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackIncidentRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackIncidentRequestValidator(): FallbackIncidentRequestValidator {
    return FallbackIncidentRequestValidator(
        mutableListOf<FallbackIncidentRequestRule>(
            FallbackIncidentRequestCodeLengthRule(1000),
            FallbackIncidentRequestRequiredOwnerIdRule("fallback incident request needs an entry"),
            FallbackIncidentRequestRequiredCodeRule("fallback incident request needs a value"),
            FallbackIncidentRequestEndsAtRangeRule(32, 255),
            FallbackIncidentRequestRequiredLabelRule("fallback incident request needs a value"),
            FallbackIncidentRequestWeightRangeRule(6.2, 866.8),
        ),
    )
}
