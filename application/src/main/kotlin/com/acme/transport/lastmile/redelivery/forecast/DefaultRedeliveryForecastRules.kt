package com.acme.transport.lastmile.redelivery.forecast

/** A rule a default redelivery forecast must pass. Check returns its problems, or none. */
interface DefaultRedeliveryForecastRule {
    fun check(item: DefaultRedeliveryForecast): MutableList<String>
}

data class DefaultRedeliveryForecastRequiredTitleRule(var message: String) : DefaultRedeliveryForecastRule {
    override fun check(item: DefaultRedeliveryForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRedeliveryForecastRequiredCarrierRule(var message: String) : DefaultRedeliveryForecastRule {
    override fun check(item: DefaultRedeliveryForecast): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRedeliveryForecastRequiredCodeRule(var message: String) : DefaultRedeliveryForecastRule {
    override fun check(item: DefaultRedeliveryForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRedeliveryForecastTitleLengthRule(var maxLength: Int) : DefaultRedeliveryForecastRule {
    override fun check(item: DefaultRedeliveryForecast): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRedeliveryForecastArchivedGuardRule(var limit: Int) : DefaultRedeliveryForecastRule {
    override fun check(item: DefaultRedeliveryForecast): MutableList<String> {
        if (item.archived && item.updatedAt > limit) {
            return mutableListOf<String>("updated at ${item.updatedAt} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRedeliveryForecastCodeLengthRule(var maxLength: Int) : DefaultRedeliveryForecastRule {
    override fun check(item: DefaultRedeliveryForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default redelivery forecast. */
data class DefaultRedeliveryForecastValidator(var rules: MutableList<DefaultRedeliveryForecastRule>) {
    fun validate(item: DefaultRedeliveryForecast): MutableList<String> {
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

    fun requireValid(item: DefaultRedeliveryForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRedeliveryForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRedeliveryForecastValidator(): DefaultRedeliveryForecastValidator {
    return DefaultRedeliveryForecastValidator(
        mutableListOf<DefaultRedeliveryForecastRule>(
            DefaultRedeliveryForecastRequiredTitleRule("default redelivery forecast needs a value"),
            DefaultRedeliveryForecastRequiredCarrierRule("default redelivery forecast needs an entry"),
            DefaultRedeliveryForecastRequiredCodeRule("default redelivery forecast needs this field"),
            DefaultRedeliveryForecastTitleLengthRule(3600),
            DefaultRedeliveryForecastArchivedGuardRule(80),
            DefaultRedeliveryForecastCodeLengthRule(120),
        ),
    )
}
