package com.acme.transport.emissions.emissionfactor.factortarget

/** A rule a fallback emission factor reservation must pass. Check returns its problems, or none. */
interface FallbackEmissionFactorReservationRule {
    fun check(item: FallbackEmissionFactorReservation): MutableList<String>
}

data class FallbackEmissionFactorReservationRequiredCodeRule(
    var message: String,
) : FallbackEmissionFactorReservationRule {
    override fun check(item: FallbackEmissionFactorReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackEmissionFactorReservationCodeLengthRule(
    var maxLength: Int,
) : FallbackEmissionFactorReservationRule {
    override fun check(item: FallbackEmissionFactorReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEmissionFactorReservationAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : FallbackEmissionFactorReservationRule {
    override fun check(item: FallbackEmissionFactorReservation): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class FallbackEmissionFactorReservationArchivedGuardRule(
    var limit: Int,
) : FallbackEmissionFactorReservationRule {
    override fun check(item: FallbackEmissionFactorReservation): MutableList<String> {
        if (item.archived && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEmissionFactorReservationNameLengthRule(
    var maxLength: Int,
) : FallbackEmissionFactorReservationRule {
    override fun check(item: FallbackEmissionFactorReservation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEmissionFactorReservationEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : FallbackEmissionFactorReservationRule {
    override fun check(item: FallbackEmissionFactorReservation): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback emission factor reservation. */
data class FallbackEmissionFactorReservationValidator(
    var rules: MutableList<FallbackEmissionFactorReservationRule>,
) {
    fun validate(item: FallbackEmissionFactorReservation): MutableList<String> {
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

    fun requireValid(item: FallbackEmissionFactorReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackEmissionFactorReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackEmissionFactorReservationValidator(): FallbackEmissionFactorReservationValidator {
    return FallbackEmissionFactorReservationValidator(
        mutableListOf<FallbackEmissionFactorReservationRule>(
            FallbackEmissionFactorReservationRequiredCodeRule(
                "fallback emission factor reservation needs an entry",
            ),
            FallbackEmissionFactorReservationCodeLengthRule(16),
            FallbackEmissionFactorReservationAttemptsRangeRule(3600, 3600),
            FallbackEmissionFactorReservationArchivedGuardRule(32),
            FallbackEmissionFactorReservationNameLengthRule(120),
            FallbackEmissionFactorReservationEtaMinutesRangeRule(255, 3600),
        ),
    )
}
