package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a cross dock reservation must pass. Check returns its problems, or none. */
interface CrossDockReservationRule {
    fun check(item: CrossDockReservation): MutableList<String>
}

data class CrossDockReservationTitleLengthRule(var maxLength: Int) : CrossDockReservationRule {
    override fun check(item: CrossDockReservation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockReservationSourceLengthRule(var maxLength: Int) : CrossDockReservationRule {
    override fun check(item: CrossDockReservation): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockReservationDepotLengthRule(var maxLength: Int) : CrossDockReservationRule {
    override fun check(item: CrossDockReservation): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockReservationRetriesRangeRule(var min: Int, var max: Int) : CrossDockReservationRule {
    override fun check(item: CrossDockReservation): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockReservationCodeLengthRule(var maxLength: Int) : CrossDockReservationRule {
    override fun check(item: CrossDockReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockReservationAllowedCodeRule(var allowed: MutableList<String>) : CrossDockReservationRule {
    override fun check(item: CrossDockReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock reservation. */
data class CrossDockReservationValidator(var rules: MutableList<CrossDockReservationRule>) {
    fun validate(item: CrossDockReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockReservationValidator(): CrossDockReservationValidator {
    return CrossDockReservationValidator(
        mutableListOf<CrossDockReservationRule>(
            CrossDockReservationTitleLengthRule(16),
            CrossDockReservationSourceLengthRule(86400),
            CrossDockReservationDepotLengthRule(80),
            CrossDockReservationRetriesRangeRule(32, 3600),
            CrossDockReservationCodeLengthRule(1000),
            CrossDockReservationAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
