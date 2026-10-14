package com.acme.transport.lastmile.parcel.reservation

/** A rule a parcel reservation must pass. Check returns its problems, or none. */
interface ParcelReservationRule {
    fun check(item: ParcelReservation): MutableList<String>
}

data class ParcelReservationScoreRangeRule(var min: Double, var max: Double) : ParcelReservationRule {
    override fun check(item: ParcelReservation): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ParcelReservationArchivedGuardRule(var limit: Int) : ParcelReservationRule {
    override fun check(item: ParcelReservation): MutableList<String> {
        if (item.archived && item.capacity > limit) {
            return mutableListOf<String>("capacity ${item.capacity} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class ParcelReservationAttemptsRangeRule(var min: Int, var max: Int) : ParcelReservationRule {
    override fun check(item: ParcelReservation): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ParcelReservationSegmentLengthRule(var maxLength: Int) : ParcelReservationRule {
    override fun check(item: ParcelReservation): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelReservationRequiredSegmentRule(var message: String) : ParcelReservationRule {
    override fun check(item: ParcelReservation): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelReservationRequiredChecksumRule(var message: String) : ParcelReservationRule {
    override fun check(item: ParcelReservation): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel reservation. */
data class ParcelReservationValidator(var rules: MutableList<ParcelReservationRule>) {
    fun validate(item: ParcelReservation): MutableList<String> {
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

    fun requireValid(item: ParcelReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelReservationValidator(): ParcelReservationValidator {
    return ParcelReservationValidator(
        mutableListOf<ParcelReservationRule>(
            ParcelReservationScoreRangeRule(1.7, 4848.5),
            ParcelReservationArchivedGuardRule(86400),
            ParcelReservationAttemptsRangeRule(120, 3600),
            ParcelReservationSegmentLengthRule(16),
            ParcelReservationRequiredSegmentRule("parcel reservation needs an entry"),
            ParcelReservationRequiredChecksumRule("parcel reservation needs this field"),
        ),
    )
}
