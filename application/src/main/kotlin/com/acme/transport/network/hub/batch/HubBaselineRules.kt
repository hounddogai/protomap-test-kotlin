package com.acme.transport.network.hub.batch

/** A rule a hub baseline must pass. Check returns its problems, or none. */
interface HubBaselineRule {
    fun check(item: HubBaseline): MutableList<String>
}

data class HubBaselineRequiredTitleRule(var message: String) : HubBaselineRule {
    override fun check(item: HubBaseline): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubBaselineContactEmailLengthRule(var maxLength: Int) : HubBaselineRule {
    override fun check(item: HubBaseline): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubBaselineRequiredCodeRule(var message: String) : HubBaselineRule {
    override fun check(item: HubBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubBaselineCodeLengthRule(var maxLength: Int) : HubBaselineRule {
    override fun check(item: HubBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubBaselineScoreRangeRule(var min: Double, var max: Double) : HubBaselineRule {
    override fun check(item: HubBaseline): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub baseline. */
data class HubBaselineValidator(var rules: MutableList<HubBaselineRule>) {
    fun validate(item: HubBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HubBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultHubBaselineValidator(): HubBaselineValidator {
    return HubBaselineValidator(
        mutableListOf<HubBaselineRule>(
            HubBaselineRequiredTitleRule("hub baseline needs an entry"),
            HubBaselineContactEmailLengthRule(64),
            HubBaselineRequiredCodeRule("hub baseline needs an entry"),
            HubBaselineCodeLengthRule(32),
            HubBaselineScoreRangeRule(7.2, 4815.5),
        ),
    )
}
