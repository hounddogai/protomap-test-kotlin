package com.acme.transport.lastmile.drop.reservation

/** A rule a drop reservation must pass. Check returns its problems, or none. */
interface DropReservationRule {
    fun check(item: DropReservation): MutableList<String>
}

data class DropReservationCodeLengthRule(var maxLength: Int) : DropReservationRule {
    override fun check(item: DropReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropReservationRequiredLabelRule(var message: String) : DropReservationRule {
    override fun check(item: DropReservation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropReservationRequiredCodeRule(var message: String) : DropReservationRule {
    override fun check(item: DropReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropReservationRequiredNameRule(var message: String) : DropReservationRule {
    override fun check(item: DropReservation): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop reservation. */
data class DropReservationValidator(var rules: MutableList<DropReservationRule>) {
    fun validate(item: DropReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DropReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultDropReservationValidator(): DropReservationValidator {
    return DropReservationValidator(
        mutableListOf<DropReservationRule>(
            DropReservationCodeLengthRule(255),
            DropReservationRequiredLabelRule("drop reservation needs an entry"),
            DropReservationRequiredCodeRule("drop reservation needs an entry"),
            DropReservationRequiredNameRule("drop reservation needs this field"),
        ),
    )
}
