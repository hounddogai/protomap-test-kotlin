package com.acme.transport.terminal.dockdoor.template

/** A rule a primary dock door reservation must pass. Check returns its problems, or none. */
interface PrimaryDockDoorReservationRule {
    fun check(item: PrimaryDockDoorReservation): MutableList<String>
}

data class PrimaryDockDoorReservationCodePrefixRule(var prefix: String) : PrimaryDockDoorReservationRule {
    override fun check(item: PrimaryDockDoorReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDockDoorReservationRequiredLabelRule(var message: String) : PrimaryDockDoorReservationRule {
    override fun check(item: PrimaryDockDoorReservation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDockDoorReservationSegmentLengthRule(var maxLength: Int) : PrimaryDockDoorReservationRule {
    override fun check(item: PrimaryDockDoorReservation): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDockDoorReservationRequiredCodeRule(var message: String) : PrimaryDockDoorReservationRule {
    override fun check(item: PrimaryDockDoorReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDockDoorReservationRequiredSegmentRule(var message: String) : PrimaryDockDoorReservationRule {
    override fun check(item: PrimaryDockDoorReservation): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary dock door reservation. */
data class PrimaryDockDoorReservationValidator(var rules: MutableList<PrimaryDockDoorReservationRule>) {
    fun validate(item: PrimaryDockDoorReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryDockDoorReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryDockDoorReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryDockDoorReservationValidator(): PrimaryDockDoorReservationValidator {
    return PrimaryDockDoorReservationValidator(
        mutableListOf<PrimaryDockDoorReservationRule>(
            PrimaryDockDoorReservationCodePrefixRule("Q"),
            PrimaryDockDoorReservationRequiredLabelRule("primary dock door reservation needs this field"),
            PrimaryDockDoorReservationSegmentLengthRule(32),
            PrimaryDockDoorReservationRequiredCodeRule("primary dock door reservation needs an entry"),
            PrimaryDockDoorReservationRequiredSegmentRule("primary dock door reservation needs an entry"),
        ),
    )
}
