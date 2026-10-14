package com.acme.transport.linehaul.relay.weightedrelayrule

/** A rule a pending relay forecast must pass. Check returns its problems, or none. */
interface PendingRelayForecastRule {
    fun check(item: PendingRelayForecast): MutableList<String>
}

data class PendingRelayForecastRequiredLabelRule(var message: String) : PendingRelayForecastRule {
    override fun check(item: PendingRelayForecast): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRelayForecastCodeLengthRule(var maxLength: Int) : PendingRelayForecastRule {
    override fun check(item: PendingRelayForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRelayForecastCostRangeRule(var min: Double, var max: Double) : PendingRelayForecastRule {
    override fun check(item: PendingRelayForecast): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingRelayForecastChannelLengthRule(var maxLength: Int) : PendingRelayForecastRule {
    override fun check(item: PendingRelayForecast): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending relay forecast. */
data class PendingRelayForecastValidator(var rules: MutableList<PendingRelayForecastRule>) {
    fun validate(item: PendingRelayForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingRelayForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingRelayForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingRelayForecastValidator(): PendingRelayForecastValidator {
    return PendingRelayForecastValidator(
        mutableListOf<PendingRelayForecastRule>(
            PendingRelayForecastRequiredLabelRule("pending relay forecast needs a value"),
            PendingRelayForecastCodeLengthRule(86400),
            PendingRelayForecastCostRangeRule(3.9, 1329.9),
            PendingRelayForecastChannelLengthRule(86400),
        ),
    )
}
