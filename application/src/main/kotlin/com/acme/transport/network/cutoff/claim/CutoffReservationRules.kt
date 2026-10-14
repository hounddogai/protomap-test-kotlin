package com.acme.transport.network.cutoff.claim

import com.acme.transport.kit.text.normalize

/** A rule a cutoff reservation must pass. Check returns its problems, or none. */
interface CutoffReservationRule {
    fun check(item: CutoffReservation): MutableList<String>
}

data class CutoffReservationExternalRefLengthRule(var maxLength: Int) : CutoffReservationRule {
    override fun check(item: CutoffReservation): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffReservationRequiredCategoryRule(var message: String) : CutoffReservationRule {
    override fun check(item: CutoffReservation): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffReservationAllowedExternalRefRule(var allowed: MutableList<String>) : CutoffReservationRule {
    override fun check(item: CutoffReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class CutoffReservationAttemptsRangeRule(var min: Int, var max: Int) : CutoffReservationRule {
    override fun check(item: CutoffReservation): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CutoffReservationRequiredExternalRefRule(var message: String) : CutoffReservationRule {
    override fun check(item: CutoffReservation): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffReservationRequiredLabelRule(var message: String) : CutoffReservationRule {
    override fun check(item: CutoffReservation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff reservation. */
data class CutoffReservationValidator(var rules: MutableList<CutoffReservationRule>) {
    fun validate(item: CutoffReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CutoffReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffReservationValidator(): CutoffReservationValidator {
    return CutoffReservationValidator(
        mutableListOf<CutoffReservationRule>(
            CutoffReservationExternalRefLengthRule(86400),
            CutoffReservationRequiredCategoryRule("cutoff reservation needs a value"),
            CutoffReservationAllowedExternalRefRule(mutableListOf<String>("rolling", "seasonal")),
            CutoffReservationAttemptsRangeRule(64, 80),
            CutoffReservationRequiredExternalRefRule("cutoff reservation needs this field"),
            CutoffReservationRequiredLabelRule("cutoff reservation needs this field"),
        ),
    )
}
