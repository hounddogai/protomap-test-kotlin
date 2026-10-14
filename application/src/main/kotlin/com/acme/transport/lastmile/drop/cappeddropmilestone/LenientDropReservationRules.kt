package com.acme.transport.lastmile.drop.cappeddropmilestone

/** A rule a lenient drop reservation must pass. Check returns its problems, or none. */
interface LenientDropReservationRule {
    fun check(item: LenientDropReservation): MutableList<String>
}

data class LenientDropReservationBatchSizeRangeRule(var min: Int, var max: Int) : LenientDropReservationRule {
    override fun check(item: LenientDropReservation): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientDropReservationRouteCodeLengthRule(var maxLength: Int) : LenientDropReservationRule {
    override fun check(item: LenientDropReservation): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDropReservationCodeLengthRule(var maxLength: Int) : LenientDropReservationRule {
    override fun check(item: LenientDropReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDropReservationSequenceRangeRule(var min: Int, var max: Int) : LenientDropReservationRule {
    override fun check(item: LenientDropReservation): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class LenientDropReservationRequiredCodeRule(var message: String) : LenientDropReservationRule {
    override fun check(item: LenientDropReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDropReservationRequiredRouteCodeRule(var message: String) : LenientDropReservationRule {
    override fun check(item: LenientDropReservation): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient drop reservation. */
data class LenientDropReservationValidator(var rules: MutableList<LenientDropReservationRule>) {
    fun validate(item: LenientDropReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientDropReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDropReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDropReservationValidator(): LenientDropReservationValidator {
    return LenientDropReservationValidator(
        mutableListOf<LenientDropReservationRule>(
            LenientDropReservationBatchSizeRangeRule(32, 120),
            LenientDropReservationRouteCodeLengthRule(16),
            LenientDropReservationCodeLengthRule(80),
            LenientDropReservationSequenceRangeRule(80, 1000),
            LenientDropReservationRequiredCodeRule("lenient drop reservation needs a value"),
            LenientDropReservationRequiredRouteCodeRule("lenient drop reservation needs this field"),
        ),
    )
}
