package com.acme.transport.linehaul.tender.digest

import com.acme.transport.kit.text.normalize

/** A rule a strict tender reservation must pass. Check returns its problems, or none. */
interface StrictTenderReservationRule {
    fun check(item: StrictTenderReservation): MutableList<String>
}

data class StrictTenderReservationCodeLengthRule(var maxLength: Int) : StrictTenderReservationRule {
    override fun check(item: StrictTenderReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTenderReservationLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : StrictTenderReservationRule {
    override fun check(item: StrictTenderReservation): MutableList<String> {
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

data class StrictTenderReservationTitleLengthRule(var maxLength: Int) : StrictTenderReservationRule {
    override fun check(item: StrictTenderReservation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTenderReservationAllowedCodeRule(
    var allowed: MutableList<String>,
) : StrictTenderReservationRule {
    override fun check(item: StrictTenderReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict tender reservation. */
data class StrictTenderReservationValidator(var rules: MutableList<StrictTenderReservationRule>) {
    fun validate(item: StrictTenderReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: StrictTenderReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTenderReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTenderReservationValidator(): StrictTenderReservationValidator {
    return StrictTenderReservationValidator(
        mutableListOf<StrictTenderReservationRule>(
            StrictTenderReservationCodeLengthRule(255),
            StrictTenderReservationLoadKgRangeRule(6.1, 4386.3),
            StrictTenderReservationTitleLengthRule(86400),
            StrictTenderReservationAllowedCodeRule(mutableListOf<String>("regional", "strict")),
        ),
    )
}
