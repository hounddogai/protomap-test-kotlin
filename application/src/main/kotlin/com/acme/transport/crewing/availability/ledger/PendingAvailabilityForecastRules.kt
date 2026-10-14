package com.acme.transport.crewing.availability.ledger

/** A rule a pending availability forecast must pass. Check returns its problems, or none. */
interface PendingAvailabilityForecastRule {
    fun check(item: PendingAvailabilityForecast): MutableList<String>
}

data class PendingAvailabilityForecastRequiredChannelRule(
    var message: String,
) : PendingAvailabilityForecastRule {
    override fun check(item: PendingAvailabilityForecast): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAvailabilityForecastUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : PendingAvailabilityForecastRule {
    override fun check(item: PendingAvailabilityForecast): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingAvailabilityForecastEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : PendingAvailabilityForecastRule {
    override fun check(item: PendingAvailabilityForecast): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class PendingAvailabilityForecastRequiredCodeRule(var message: String) : PendingAvailabilityForecastRule {
    override fun check(item: PendingAvailabilityForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAvailabilityForecastCodeLengthRule(var maxLength: Int) : PendingAvailabilityForecastRule {
    override fun check(item: PendingAvailabilityForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingAvailabilityForecastCeilingRangeRule(
    var min: Double,
    var max: Double,
) : PendingAvailabilityForecastRule {
    override fun check(item: PendingAvailabilityForecast): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a pending availability forecast. */
data class PendingAvailabilityForecastValidator(var rules: MutableList<PendingAvailabilityForecastRule>) {
    fun validate(item: PendingAvailabilityForecast): MutableList<String> {
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

    fun requireValid(item: PendingAvailabilityForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingAvailabilityForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingAvailabilityForecastValidator(): PendingAvailabilityForecastValidator {
    return PendingAvailabilityForecastValidator(
        mutableListOf<PendingAvailabilityForecastRule>(
            PendingAvailabilityForecastRequiredChannelRule("pending availability forecast needs this field"),
            PendingAvailabilityForecastUpdatedAtRangeRule(120, 1000),
            PendingAvailabilityForecastEndsAtRangeRule(80, 120),
            PendingAvailabilityForecastRequiredCodeRule("pending availability forecast needs this field"),
            PendingAvailabilityForecastCodeLengthRule(3600),
            PendingAvailabilityForecastCeilingRangeRule(2.5, 2270.8),
        ),
    )
}
