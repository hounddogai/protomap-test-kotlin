package com.acme.transport.customs.broker.overrideitem

/** A rule a broker forecast must pass. Check returns its problems, or none. */
interface BrokerForecastRule {
    fun check(item: BrokerForecast): MutableList<String>
}

data class BrokerForecastLabelLengthRule(var maxLength: Int) : BrokerForecastRule {
    override fun check(item: BrokerForecast): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerForecastRequiredCodeRule(var message: String) : BrokerForecastRule {
    override fun check(item: BrokerForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerForecastCodePrefixRule(var prefix: String) : BrokerForecastRule {
    override fun check(item: BrokerForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BrokerForecastNameLengthRule(var maxLength: Int) : BrokerForecastRule {
    override fun check(item: BrokerForecast): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerForecastCodeLengthRule(var maxLength: Int) : BrokerForecastRule {
    override fun check(item: BrokerForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker forecast. */
data class BrokerForecastValidator(var rules: MutableList<BrokerForecastRule>) {
    fun validate(item: BrokerForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BrokerForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerForecastValidator(): BrokerForecastValidator {
    return BrokerForecastValidator(
        mutableListOf<BrokerForecastRule>(
            BrokerForecastLabelLengthRule(86400),
            BrokerForecastRequiredCodeRule("broker forecast needs this field"),
            BrokerForecastCodePrefixRule("X"),
            BrokerForecastNameLengthRule(64),
            BrokerForecastCodeLengthRule(255),
        ),
    )
}
