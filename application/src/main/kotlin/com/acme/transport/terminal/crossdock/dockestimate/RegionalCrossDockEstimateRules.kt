package com.acme.transport.terminal.crossdock.dockestimate

/** A rule a regional cross dock estimate must pass. Check returns its problems, or none. */
interface RegionalCrossDockEstimateRule {
    fun check(item: RegionalCrossDockEstimate): MutableList<String>
}

data class RegionalCrossDockEstimateRequiredCodeRule(var message: String) : RegionalCrossDockEstimateRule {
    override fun check(item: RegionalCrossDockEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCrossDockEstimatePhoneLengthRule(var maxLength: Int) : RegionalCrossDockEstimateRule {
    override fun check(item: RegionalCrossDockEstimate): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCrossDockEstimateStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : RegionalCrossDockEstimateRule {
    override fun check(item: RegionalCrossDockEstimate): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCrossDockEstimateNameLengthRule(var maxLength: Int) : RegionalCrossDockEstimateRule {
    override fun check(item: RegionalCrossDockEstimate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCrossDockEstimateCodeLengthRule(var maxLength: Int) : RegionalCrossDockEstimateRule {
    override fun check(item: RegionalCrossDockEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional cross dock estimate. */
data class RegionalCrossDockEstimateValidator(var rules: MutableList<RegionalCrossDockEstimateRule>) {
    fun validate(item: RegionalCrossDockEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalCrossDockEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCrossDockEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCrossDockEstimateValidator(): RegionalCrossDockEstimateValidator {
    return RegionalCrossDockEstimateValidator(
        mutableListOf<RegionalCrossDockEstimateRule>(
            RegionalCrossDockEstimateRequiredCodeRule("regional cross dock estimate needs a value"),
            RegionalCrossDockEstimatePhoneLengthRule(120),
            RegionalCrossDockEstimateStartsAtRangeRule(1000, 86400),
            RegionalCrossDockEstimateNameLengthRule(120),
            RegionalCrossDockEstimateCodeLengthRule(80),
        ),
    )
}
