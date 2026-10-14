package com.acme.transport.lastmile.redelivery.redeliveryentry

/** A rule a seasonal redelivery reservation must pass. Check returns its problems, or none. */
interface SeasonalRedeliveryReservationRule {
    fun check(item: SeasonalRedeliveryReservation): MutableList<String>
}

data class SeasonalRedeliveryReservationCurrencyLengthRule(
    var maxLength: Int,
) : SeasonalRedeliveryReservationRule {
    override fun check(item: SeasonalRedeliveryReservation): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliveryReservationCodePrefixRule(var prefix: String) : SeasonalRedeliveryReservationRule {
    override fun check(item: SeasonalRedeliveryReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliveryReservationTitleLengthRule(
    var maxLength: Int,
) : SeasonalRedeliveryReservationRule {
    override fun check(item: SeasonalRedeliveryReservation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliveryReservationQuantityRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalRedeliveryReservationRule {
    override fun check(item: SeasonalRedeliveryReservation): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a seasonal redelivery reservation. */
data class SeasonalRedeliveryReservationValidator(var rules: MutableList<SeasonalRedeliveryReservationRule>) {
    fun validate(item: SeasonalRedeliveryReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalRedeliveryReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRedeliveryReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRedeliveryReservationValidator(): SeasonalRedeliveryReservationValidator {
    return SeasonalRedeliveryReservationValidator(
        mutableListOf<SeasonalRedeliveryReservationRule>(
            SeasonalRedeliveryReservationCurrencyLengthRule(32),
            SeasonalRedeliveryReservationCodePrefixRule("REF"),
            SeasonalRedeliveryReservationTitleLengthRule(86400),
            SeasonalRedeliveryReservationQuantityRangeRule(32, 1000),
        ),
    )
}
