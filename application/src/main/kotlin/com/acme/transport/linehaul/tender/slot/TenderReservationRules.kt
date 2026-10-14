package com.acme.transport.linehaul.tender.slot

/** A rule a tender reservation must pass. Check returns its problems, or none. */
interface TenderReservationRule {
    fun check(item: TenderReservation): MutableList<String>
}

data class TenderReservationRequiredTitleRule(var message: String) : TenderReservationRule {
    override fun check(item: TenderReservation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderReservationCodeLengthRule(var maxLength: Int) : TenderReservationRule {
    override fun check(item: TenderReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderReservationTitleLengthRule(var maxLength: Int) : TenderReservationRule {
    override fun check(item: TenderReservation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderReservationCodePrefixRule(var prefix: String) : TenderReservationRule {
    override fun check(item: TenderReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender reservation. */
data class TenderReservationValidator(var rules: MutableList<TenderReservationRule>) {
    fun validate(item: TenderReservation): MutableList<String> {
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

    fun requireValid(item: TenderReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderReservationValidator(): TenderReservationValidator {
    return TenderReservationValidator(
        mutableListOf<TenderReservationRule>(
            TenderReservationRequiredTitleRule("tender reservation needs a value"),
            TenderReservationCodeLengthRule(1000),
            TenderReservationTitleLengthRule(86400),
            TenderReservationCodePrefixRule("OPS"),
        ),
    )
}
