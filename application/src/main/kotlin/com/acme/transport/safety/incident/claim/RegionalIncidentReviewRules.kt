package com.acme.transport.safety.incident.claim

/** A rule a regional incident review must pass. Check returns its problems, or none. */
interface RegionalIncidentReviewRule {
    fun check(item: RegionalIncidentReview): MutableList<String>
}

data class RegionalIncidentReviewDescriptionLengthRule(var maxLength: Int) : RegionalIncidentReviewRule {
    override fun check(item: RegionalIncidentReview): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentReviewRequiredVehicleIdRule(var message: String) : RegionalIncidentReviewRule {
    override fun check(item: RegionalIncidentReview): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentReviewStartsAtRangeRule(var min: Int, var max: Int) : RegionalIncidentReviewRule {
    override fun check(item: RegionalIncidentReview): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentReviewCodePrefixRule(var prefix: String) : RegionalIncidentReviewRule {
    override fun check(item: RegionalIncidentReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional incident review. */
data class RegionalIncidentReviewValidator(var rules: MutableList<RegionalIncidentReviewRule>) {
    fun validate(item: RegionalIncidentReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalIncidentReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalIncidentReview>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalIncidentReviewValidator(): RegionalIncidentReviewValidator {
    return RegionalIncidentReviewValidator(
        mutableListOf<RegionalIncidentReviewRule>(
            RegionalIncidentReviewDescriptionLengthRule(3600),
            RegionalIncidentReviewRequiredVehicleIdRule("regional incident review needs an entry"),
            RegionalIncidentReviewStartsAtRangeRule(64, 3600),
            RegionalIncidentReviewCodePrefixRule("REF"),
        ),
    )
}
