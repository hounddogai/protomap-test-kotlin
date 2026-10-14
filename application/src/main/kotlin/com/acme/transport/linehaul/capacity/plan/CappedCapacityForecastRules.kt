package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.kit.text.normalize

/** A rule a capped capacity forecast must pass. Check returns its problems, or none. */
interface CappedCapacityForecastRule {
    fun check(item: CappedCapacityForecast): MutableList<String>
}

data class CappedCapacityForecastRequiredCodeRule(var message: String) : CappedCapacityForecastRule {
    override fun check(item: CappedCapacityForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCapacityForecastAllowedDescriptionRule(
    var allowed: MutableList<String>,
) : CappedCapacityForecastRule {
    override fun check(item: CappedCapacityForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class CappedCapacityForecastArchivedGuardRule(var limit: Double) : CappedCapacityForecastRule {
    override fun check(item: CappedCapacityForecast): MutableList<String> {
        if (item.archived && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class CappedCapacityForecastAmountRangeRule(
    var min: Double,
    var max: Double,
) : CappedCapacityForecastRule {
    override fun check(item: CappedCapacityForecast): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class CappedCapacityForecastNameLengthRule(var maxLength: Int) : CappedCapacityForecastRule {
    override fun check(item: CappedCapacityForecast): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped capacity forecast. */
data class CappedCapacityForecastValidator(var rules: MutableList<CappedCapacityForecastRule>) {
    fun validate(item: CappedCapacityForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedCapacityForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCapacityForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCapacityForecastValidator(): CappedCapacityForecastValidator {
    return CappedCapacityForecastValidator(
        mutableListOf<CappedCapacityForecastRule>(
            CappedCapacityForecastRequiredCodeRule("capped capacity forecast needs an entry"),
            CappedCapacityForecastAllowedDescriptionRule(mutableListOf<String>("capped", "pending", "tiered")),
            CappedCapacityForecastArchivedGuardRule(4.2),
            CappedCapacityForecastAmountRangeRule(2.4, 3024.5),
            CappedCapacityForecastNameLengthRule(255),
        ),
    )
}
