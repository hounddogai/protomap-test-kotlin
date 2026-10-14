package com.acme.transport.network.cutoff.adjustment

/** A rule a legacy cutoff reservation must pass. Check returns its problems, or none. */
interface LegacyCutoffReservationRule {
    fun check(item: LegacyCutoffReservation): MutableList<String>
}

data class LegacyCutoffReservationRequiredLabelRule(var message: String) : LegacyCutoffReservationRule {
    override fun check(item: LegacyCutoffReservation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyCutoffReservationDiscountRangeRule(
    var min: Double,
    var max: Double,
) : LegacyCutoffReservationRule {
    override fun check(item: LegacyCutoffReservation): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCutoffReservationSummaryLengthRule(var maxLength: Int) : LegacyCutoffReservationRule {
    override fun check(item: LegacyCutoffReservation): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCutoffReservationRequiredTitleRule(var message: String) : LegacyCutoffReservationRule {
    override fun check(item: LegacyCutoffReservation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyCutoffReservationRequiredCodeRule(var message: String) : LegacyCutoffReservationRule {
    override fun check(item: LegacyCutoffReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy cutoff reservation. */
data class LegacyCutoffReservationValidator(var rules: MutableList<LegacyCutoffReservationRule>) {
    fun validate(item: LegacyCutoffReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyCutoffReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyCutoffReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyCutoffReservationValidator(): LegacyCutoffReservationValidator {
    return LegacyCutoffReservationValidator(
        mutableListOf<LegacyCutoffReservationRule>(
            LegacyCutoffReservationRequiredLabelRule("legacy cutoff reservation needs a value"),
            LegacyCutoffReservationDiscountRangeRule(4.7, 2812.8),
            LegacyCutoffReservationSummaryLengthRule(80),
            LegacyCutoffReservationRequiredTitleRule("legacy cutoff reservation needs this field"),
            LegacyCutoffReservationRequiredCodeRule("legacy cutoff reservation needs an entry"),
        ),
    )
}
