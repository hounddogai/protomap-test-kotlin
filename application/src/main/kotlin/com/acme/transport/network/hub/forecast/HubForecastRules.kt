package com.acme.transport.network.hub.forecast

import com.acme.transport.kit.text.normalize

/** A rule a hub forecast must pass. Check returns its problems, or none. */
interface HubForecastRule {
    fun check(item: HubForecast): MutableList<String>
}

data class HubForecastRequiredCodeRule(var message: String) : HubForecastRule {
    override fun check(item: HubForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubForecastAllowedChannelRule(var allowed: MutableList<String>) : HubForecastRule {
    override fun check(item: HubForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class HubForecastNameLengthRule(var maxLength: Int) : HubForecastRule {
    override fun check(item: HubForecast): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubForecastCodeLengthRule(var maxLength: Int) : HubForecastRule {
    override fun check(item: HubForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub forecast. */
data class HubForecastValidator(var rules: MutableList<HubForecastRule>) {
    fun validate(item: HubForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HubForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultHubForecastValidator(): HubForecastValidator {
    return HubForecastValidator(
        mutableListOf<HubForecastRule>(
            HubForecastRequiredCodeRule("hub forecast needs this field"),
            HubForecastAllowedChannelRule(mutableListOf<String>("regional")),
            HubForecastNameLengthRule(3600),
            HubForecastCodeLengthRule(16),
        ),
    )
}
