package com.acme.transport.linehaul.relay.summary

/** A rule a pending relay reservation must pass. Check returns its problems, or none. */
interface PendingRelayReservationRule {
    fun check(item: PendingRelayReservation): MutableList<String>
}

data class PendingRelayReservationRequiredTitleRule(var message: String) : PendingRelayReservationRule {
    override fun check(item: PendingRelayReservation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRelayReservationEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : PendingRelayReservationRule {
    override fun check(item: PendingRelayReservation): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingRelayReservationUpdatedAtRangeRule(var min: Int, var max: Int) : PendingRelayReservationRule {
    override fun check(item: PendingRelayReservation): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingRelayReservationCodeLengthRule(var maxLength: Int) : PendingRelayReservationRule {
    override fun check(item: PendingRelayReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRelayReservationTitleLengthRule(var maxLength: Int) : PendingRelayReservationRule {
    override fun check(item: PendingRelayReservation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRelayReservationVersionRangeRule(var min: Int, var max: Int) : PendingRelayReservationRule {
    override fun check(item: PendingRelayReservation): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a pending relay reservation. */
data class PendingRelayReservationValidator(var rules: MutableList<PendingRelayReservationRule>) {
    fun validate(item: PendingRelayReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingRelayReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingRelayReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingRelayReservationValidator(): PendingRelayReservationValidator {
    return PendingRelayReservationValidator(
        mutableListOf<PendingRelayReservationRule>(
            PendingRelayReservationRequiredTitleRule("pending relay reservation needs this field"),
            PendingRelayReservationEtaMinutesRangeRule(32, 255),
            PendingRelayReservationUpdatedAtRangeRule(255, 3600),
            PendingRelayReservationCodeLengthRule(80),
            PendingRelayReservationTitleLengthRule(120),
            PendingRelayReservationVersionRangeRule(1000, 86400),
        ),
    )
}
