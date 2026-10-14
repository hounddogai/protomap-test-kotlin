package com.acme.transport.freightpay.dispute.disputeforecast

import com.acme.transport.kit.text.normalize

/** A rule a tiered dispute forecast must pass. Check returns its problems, or none. */
interface TieredDisputeForecastRule {
    fun check(item: TieredDisputeForecast): MutableList<String>
}

data class TieredDisputeForecastAllowedChannelRule(
    var allowed: MutableList<String>,
) : TieredDisputeForecastRule {
    override fun check(item: TieredDisputeForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class TieredDisputeForecastRequiredCodeRule(var message: String) : TieredDisputeForecastRule {
    override fun check(item: TieredDisputeForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDisputeForecastRequiredChannelRule(var message: String) : TieredDisputeForecastRule {
    override fun check(item: TieredDisputeForecast): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDisputeForecastNameLengthRule(var maxLength: Int) : TieredDisputeForecastRule {
    override fun check(item: TieredDisputeForecast): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered dispute forecast. */
data class TieredDisputeForecastValidator(var rules: MutableList<TieredDisputeForecastRule>) {
    fun validate(item: TieredDisputeForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredDisputeForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDisputeForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDisputeForecastValidator(): TieredDisputeForecastValidator {
    return TieredDisputeForecastValidator(
        mutableListOf<TieredDisputeForecastRule>(
            TieredDisputeForecastAllowedChannelRule(mutableListOf<String>("adaptive")),
            TieredDisputeForecastRequiredCodeRule("tiered dispute forecast needs an entry"),
            TieredDisputeForecastRequiredChannelRule("tiered dispute forecast needs this field"),
            TieredDisputeForecastNameLengthRule(64),
        ),
    )
}
