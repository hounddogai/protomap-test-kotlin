package com.acme.transport.customs.broker.group

/** A rule a seasonal broker group must pass. Check returns its problems, or none. */
interface SeasonalBrokerGroupRule {
    fun check(item: SeasonalBrokerGroup): MutableList<String>
}

data class SeasonalBrokerGroupContactEmailLengthRule(var maxLength: Int) : SeasonalBrokerGroupRule {
    override fun check(item: SeasonalBrokerGroup): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalBrokerGroupDistanceKmRangeRule(var min: Double, var max: Double) : SeasonalBrokerGroupRule {
    override fun check(item: SeasonalBrokerGroup): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalBrokerGroupRequiredNameRule(var message: String) : SeasonalBrokerGroupRule {
    override fun check(item: SeasonalBrokerGroup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalBrokerGroupNameLengthRule(var maxLength: Int) : SeasonalBrokerGroupRule {
    override fun check(item: SeasonalBrokerGroup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalBrokerGroupCodePrefixRule(var prefix: String) : SeasonalBrokerGroupRule {
    override fun check(item: SeasonalBrokerGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal broker group. */
data class SeasonalBrokerGroupValidator(var rules: MutableList<SeasonalBrokerGroupRule>) {
    fun validate(item: SeasonalBrokerGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalBrokerGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalBrokerGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalBrokerGroupValidator(): SeasonalBrokerGroupValidator {
    return SeasonalBrokerGroupValidator(
        mutableListOf<SeasonalBrokerGroupRule>(
            SeasonalBrokerGroupContactEmailLengthRule(255),
            SeasonalBrokerGroupDistanceKmRangeRule(1.3, 2376.5),
            SeasonalBrokerGroupRequiredNameRule("seasonal broker group needs a value"),
            SeasonalBrokerGroupNameLengthRule(86400),
            SeasonalBrokerGroupCodePrefixRule("OPS"),
        ),
    )
}
