package com.acme.transport.safety.violation.reservation

/** A rule a violation reservation must pass. Check returns its problems, or none. */
interface ViolationReservationRule {
    fun check(item: ViolationReservation): MutableList<String>
}

data class ViolationReservationRequiredSegmentRule(var message: String) : ViolationReservationRule {
    override fun check(item: ViolationReservation): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationReservationSegmentLengthRule(var maxLength: Int) : ViolationReservationRule {
    override fun check(item: ViolationReservation): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationReservationApprovedGuardRule(var limit: Int) : ViolationReservationRule {
    override fun check(item: ViolationReservation): MutableList<String> {
        if (item.approved && item.limit > limit) {
            return mutableListOf<String>("limit ${item.limit} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation reservation. */
data class ViolationReservationValidator(var rules: MutableList<ViolationReservationRule>) {
    fun validate(item: ViolationReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationReservationValidator(): ViolationReservationValidator {
    return ViolationReservationValidator(
        mutableListOf<ViolationReservationRule>(
            ViolationReservationRequiredSegmentRule("violation reservation needs this field"),
            ViolationReservationSegmentLengthRule(80),
            ViolationReservationApprovedGuardRule(3600),
        ),
    )
}
