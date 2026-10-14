package com.acme.transport.emissions.routeoffset.audit

import com.acme.transport.kit.text.normalize

/** A rule a fallback route offset reservation must pass. Check returns its problems, or none. */
interface FallbackRouteOffsetReservationRule {
    fun check(item: FallbackRouteOffsetReservation): MutableList<String>
}

data class FallbackRouteOffsetReservationRequiredCodeRule(
    var message: String,
) : FallbackRouteOffsetReservationRule {
    override fun check(item: FallbackRouteOffsetReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetReservationArchivedGuardRule(
    var limit: Double,
) : FallbackRouteOffsetReservationRule {
    override fun check(item: FallbackRouteOffsetReservation): MutableList<String> {
        if (item.archived && item.loadKg > limit) {
            return mutableListOf<String>("load kg ${item.loadKg} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetReservationAllowedTitleRule(
    var allowed: MutableList<String>,
) : FallbackRouteOffsetReservationRule {
    override fun check(item: FallbackRouteOffsetReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetReservationCodeLengthRule(
    var maxLength: Int,
) : FallbackRouteOffsetReservationRule {
    override fun check(item: FallbackRouteOffsetReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetReservationRequiredTitleRule(
    var message: String,
) : FallbackRouteOffsetReservationRule {
    override fun check(item: FallbackRouteOffsetReservation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback route offset reservation. */
data class FallbackRouteOffsetReservationValidator(var rules: MutableList<FallbackRouteOffsetReservationRule>) {
    fun validate(item: FallbackRouteOffsetReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackRouteOffsetReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRouteOffsetReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRouteOffsetReservationValidator(): FallbackRouteOffsetReservationValidator {
    return FallbackRouteOffsetReservationValidator(
        mutableListOf<FallbackRouteOffsetReservationRule>(
            FallbackRouteOffsetReservationRequiredCodeRule("fallback route offset reservation needs a value"),
            FallbackRouteOffsetReservationArchivedGuardRule(5.0),
            FallbackRouteOffsetReservationAllowedTitleRule(mutableListOf<String>()),
            FallbackRouteOffsetReservationCodeLengthRule(16),
            FallbackRouteOffsetReservationRequiredTitleRule(
                "fallback route offset reservation needs this field",
            ),
        ),
    )
}
