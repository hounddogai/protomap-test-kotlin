package com.acme.transport.freightpay.dispute.schedule

/** A rule a seasonal dispute reservation must pass. Check returns its problems, or none. */
interface SeasonalDisputeReservationRule {
    fun check(item: SeasonalDisputeReservation): MutableList<String>
}

data class SeasonalDisputeReservationRatioRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalDisputeReservationRule {
    override fun check(item: SeasonalDisputeReservation): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDisputeReservationCodeLengthRule(var maxLength: Int) : SeasonalDisputeReservationRule {
    override fun check(item: SeasonalDisputeReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDisputeReservationTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : SeasonalDisputeReservationRule {
    override fun check(item: SeasonalDisputeReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class SeasonalDisputeReservationCodePrefixRule(var prefix: String) : SeasonalDisputeReservationRule {
    override fun check(item: SeasonalDisputeReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDisputeReservationRateRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalDisputeReservationRule {
    override fun check(item: SeasonalDisputeReservation): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a seasonal dispute reservation. */
data class SeasonalDisputeReservationValidator(var rules: MutableList<SeasonalDisputeReservationRule>) {
    fun validate(item: SeasonalDisputeReservation): MutableList<String> {
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

    fun requireValid(item: SeasonalDisputeReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDisputeReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDisputeReservationValidator(): SeasonalDisputeReservationValidator {
    return SeasonalDisputeReservationValidator(
        mutableListOf<SeasonalDisputeReservationRule>(
            SeasonalDisputeReservationRatioRangeRule(0.8, 422.7),
            SeasonalDisputeReservationCodeLengthRule(255),
            SeasonalDisputeReservationTagsLimitRule(86400, mutableListOf<String>()),
            SeasonalDisputeReservationCodePrefixRule("Q"),
            SeasonalDisputeReservationRateRangeRule(5.5, 146.2),
        ),
    )
}
