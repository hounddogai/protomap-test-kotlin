package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.text.normalize

/** A rule a fallback backhaul reservation must pass. Check returns its problems, or none. */
interface FallbackBackhaulReservationRule {
    fun check(item: FallbackBackhaulReservation): MutableList<String>
}

data class FallbackBackhaulReservationAllowedCodeRule(
    var allowed: MutableList<String>,
) : FallbackBackhaulReservationRule {
    override fun check(item: FallbackBackhaulReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulReservationNotesLengthRule(var maxLength: Int) : FallbackBackhaulReservationRule {
    override fun check(item: FallbackBackhaulReservation): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulReservationRequiredRegionRule(
    var message: String,
) : FallbackBackhaulReservationRule {
    override fun check(item: FallbackBackhaulReservation): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulReservationPinnedGuardRule(var limit: Int) : FallbackBackhaulReservationRule {
    override fun check(item: FallbackBackhaulReservation): MutableList<String> {
        if (item.pinned && item.odometer > limit) {
            return mutableListOf<String>("odometer ${item.odometer} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulReservationRequiredNotesRule(var message: String) : FallbackBackhaulReservationRule {
    override fun check(item: FallbackBackhaulReservation): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulReservationCodeLengthRule(var maxLength: Int) : FallbackBackhaulReservationRule {
    override fun check(item: FallbackBackhaulReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback backhaul reservation. */
data class FallbackBackhaulReservationValidator(var rules: MutableList<FallbackBackhaulReservationRule>) {
    fun validate(item: FallbackBackhaulReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackBackhaulReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackBackhaulReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackBackhaulReservationValidator(): FallbackBackhaulReservationValidator {
    return FallbackBackhaulReservationValidator(
        mutableListOf<FallbackBackhaulReservationRule>(
            FallbackBackhaulReservationAllowedCodeRule(mutableListOf<String>("batched", "scheduled", "capped")),
            FallbackBackhaulReservationNotesLengthRule(64),
            FallbackBackhaulReservationRequiredRegionRule("fallback backhaul reservation needs an entry"),
            FallbackBackhaulReservationPinnedGuardRule(3600),
            FallbackBackhaulReservationRequiredNotesRule("fallback backhaul reservation needs this field"),
            FallbackBackhaulReservationCodeLengthRule(3600),
        ),
    )
}
