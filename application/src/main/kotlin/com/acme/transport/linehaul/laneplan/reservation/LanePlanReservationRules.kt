package com.acme.transport.linehaul.laneplan.reservation

/** A rule a lane plan reservation must pass. Check returns its problems, or none. */
interface LanePlanReservationRule {
    fun check(item: LanePlanReservation): MutableList<String>
}

data class LanePlanReservationChecksumLengthRule(var maxLength: Int) : LanePlanReservationRule {
    override fun check(item: LanePlanReservation): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanReservationRequiredCodeRule(var message: String) : LanePlanReservationRule {
    override fun check(item: LanePlanReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanReservationCodePrefixRule(var prefix: String) : LanePlanReservationRule {
    override fun check(item: LanePlanReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanReservationApprovedGuardRule(var limit: Double) : LanePlanReservationRule {
    override fun check(item: LanePlanReservation): MutableList<String> {
        if (item.approved && item.discount > limit) {
            return mutableListOf<String>("discount ${item.discount} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanReservationRequiredShiftCodeRule(var message: String) : LanePlanReservationRule {
    override fun check(item: LanePlanReservation): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan reservation. */
data class LanePlanReservationValidator(var rules: MutableList<LanePlanReservationRule>) {
    fun validate(item: LanePlanReservation): MutableList<String> {
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

    fun requireValid(item: LanePlanReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanReservationValidator(): LanePlanReservationValidator {
    return LanePlanReservationValidator(
        mutableListOf<LanePlanReservationRule>(
            LanePlanReservationChecksumLengthRule(64),
            LanePlanReservationRequiredCodeRule("lane plan reservation needs an entry"),
            LanePlanReservationCodePrefixRule("Q"),
            LanePlanReservationApprovedGuardRule(4.9),
            LanePlanReservationRequiredShiftCodeRule("lane plan reservation needs this field"),
        ),
    )
}
