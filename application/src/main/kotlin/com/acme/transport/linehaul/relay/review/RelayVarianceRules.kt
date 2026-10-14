package com.acme.transport.linehaul.relay.review

/** A rule a relay variance must pass. Check returns its problems, or none. */
interface RelayVarianceRule {
    fun check(item: RelayVariance): MutableList<String>
}

data class RelayVarianceCodePrefixRule(var prefix: String) : RelayVarianceRule {
    override fun check(item: RelayVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RelayVarianceDescriptionLengthRule(var maxLength: Int) : RelayVarianceRule {
    override fun check(item: RelayVariance): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayVarianceRateRangeRule(var min: Double, var max: Double) : RelayVarianceRule {
    override fun check(item: RelayVariance): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RelayVarianceRequiredNameRule(var message: String) : RelayVarianceRule {
    override fun check(item: RelayVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay variance. */
data class RelayVarianceValidator(var rules: MutableList<RelayVarianceRule>) {
    fun validate(item: RelayVariance): MutableList<String> {
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

    fun requireValid(item: RelayVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayVarianceValidator(): RelayVarianceValidator {
    return RelayVarianceValidator(
        mutableListOf<RelayVarianceRule>(
            RelayVarianceCodePrefixRule("OPS"),
            RelayVarianceDescriptionLengthRule(255),
            RelayVarianceRateRangeRule(0.9, 206.1),
            RelayVarianceRequiredNameRule("relay variance needs this field"),
        ),
    )
}
