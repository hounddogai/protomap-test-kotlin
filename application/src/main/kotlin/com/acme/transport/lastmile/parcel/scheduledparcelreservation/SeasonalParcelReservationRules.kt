package com.acme.transport.lastmile.parcel.scheduledparcelreservation

/** A rule a seasonal parcel reservation must pass. Check returns its problems, or none. */
interface SeasonalParcelReservationRule {
    fun check(item: SeasonalParcelReservation): MutableList<String>
}

data class SeasonalParcelReservationRequiredCodeRule(var message: String) : SeasonalParcelReservationRule {
    override fun check(item: SeasonalParcelReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalParcelReservationCodePrefixRule(var prefix: String) : SeasonalParcelReservationRule {
    override fun check(item: SeasonalParcelReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalParcelReservationNameLengthRule(var maxLength: Int) : SeasonalParcelReservationRule {
    override fun check(item: SeasonalParcelReservation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalParcelReservationNotesLengthRule(var maxLength: Int) : SeasonalParcelReservationRule {
    override fun check(item: SeasonalParcelReservation): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal parcel reservation. */
data class SeasonalParcelReservationValidator(var rules: MutableList<SeasonalParcelReservationRule>) {
    fun validate(item: SeasonalParcelReservation): MutableList<String> {
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

    fun requireValid(item: SeasonalParcelReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalParcelReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalParcelReservationValidator(): SeasonalParcelReservationValidator {
    return SeasonalParcelReservationValidator(
        mutableListOf<SeasonalParcelReservationRule>(
            SeasonalParcelReservationRequiredCodeRule("seasonal parcel reservation needs a value"),
            SeasonalParcelReservationCodePrefixRule("X"),
            SeasonalParcelReservationNameLengthRule(64),
            SeasonalParcelReservationNotesLengthRule(120),
        ),
    )
}
