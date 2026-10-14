package com.acme.transport.linehaul.backhaul.reservation

/** A rule a backhaul reservation must pass. Check returns its problems, or none. */
interface BackhaulReservationRule {
    fun check(item: BackhaulReservation): MutableList<String>
}

data class BackhaulReservationCodeLengthRule(var maxLength: Int) : BackhaulReservationRule {
    override fun check(item: BackhaulReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulReservationCountryLengthRule(var maxLength: Int) : BackhaulReservationRule {
    override fun check(item: BackhaulReservation): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulReservationTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : BackhaulReservationRule {
    override fun check(item: BackhaulReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class BackhaulReservationRequiredCodeRule(var message: String) : BackhaulReservationRule {
    override fun check(item: BackhaulReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulReservationArchivedGuardRule(var limit: Double) : BackhaulReservationRule {
    override fun check(item: BackhaulReservation): MutableList<String> {
        if (item.archived && item.budget > limit) {
            return mutableListOf<String>("budget ${item.budget} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulReservationStartsAtRangeRule(var min: Int, var max: Int) : BackhaulReservationRule {
    override fun check(item: BackhaulReservation): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul reservation. */
data class BackhaulReservationValidator(var rules: MutableList<BackhaulReservationRule>) {
    fun validate(item: BackhaulReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BackhaulReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulReservationValidator(): BackhaulReservationValidator {
    return BackhaulReservationValidator(
        mutableListOf<BackhaulReservationRule>(
            BackhaulReservationCodeLengthRule(1000),
            BackhaulReservationCountryLengthRule(86400),
            BackhaulReservationTagsLimitRule(86400, mutableListOf<String>("weighted", "scheduled", "manual")),
            BackhaulReservationRequiredCodeRule("backhaul reservation needs a value"),
            BackhaulReservationArchivedGuardRule(8.3),
            BackhaulReservationStartsAtRangeRule(16, 64),
        ),
    )
}
