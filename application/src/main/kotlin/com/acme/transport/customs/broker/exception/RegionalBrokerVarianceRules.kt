package com.acme.transport.customs.broker.exception

import com.acme.transport.kit.text.normalize

/** A rule a regional broker variance must pass. Check returns its problems, or none. */
interface RegionalBrokerVarianceRule {
    fun check(item: RegionalBrokerVariance): MutableList<String>
}

data class RegionalBrokerVarianceEtaMinutesRangeRule(var min: Int, var max: Int) : RegionalBrokerVarianceRule {
    override fun check(item: RegionalBrokerVariance): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalBrokerVarianceCodeLengthRule(var maxLength: Int) : RegionalBrokerVarianceRule {
    override fun check(item: RegionalBrokerVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalBrokerVarianceRequiredCodeRule(var message: String) : RegionalBrokerVarianceRule {
    override fun check(item: RegionalBrokerVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalBrokerVarianceRouteCodeLengthRule(var maxLength: Int) : RegionalBrokerVarianceRule {
    override fun check(item: RegionalBrokerVariance): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalBrokerVarianceAllowedTitleRule(
    var allowed: MutableList<String>,
) : RegionalBrokerVarianceRule {
    override fun check(item: RegionalBrokerVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional broker variance. */
data class RegionalBrokerVarianceValidator(var rules: MutableList<RegionalBrokerVarianceRule>) {
    fun validate(item: RegionalBrokerVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalBrokerVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalBrokerVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalBrokerVarianceValidator(): RegionalBrokerVarianceValidator {
    return RegionalBrokerVarianceValidator(
        mutableListOf<RegionalBrokerVarianceRule>(
            RegionalBrokerVarianceEtaMinutesRangeRule(16, 32),
            RegionalBrokerVarianceCodeLengthRule(120),
            RegionalBrokerVarianceRequiredCodeRule("regional broker variance needs an entry"),
            RegionalBrokerVarianceRouteCodeLengthRule(86400),
            RegionalBrokerVarianceAllowedTitleRule(mutableListOf<String>("manual", "weighted")),
        ),
    )
}
