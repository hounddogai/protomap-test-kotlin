package com.acme.transport.customs.broker.entry

/** A rule a weighted broker score must pass. Check returns its problems, or none. */
interface WeightedBrokerScoreRule {
    fun check(item: WeightedBrokerScore): MutableList<String>
}

data class WeightedBrokerScoreRequiredContactEmailRule(var message: String) : WeightedBrokerScoreRule {
    override fun check(item: WeightedBrokerScore): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedBrokerScoreCodeLengthRule(var maxLength: Int) : WeightedBrokerScoreRule {
    override fun check(item: WeightedBrokerScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedBrokerScoreLabelLengthRule(var maxLength: Int) : WeightedBrokerScoreRule {
    override fun check(item: WeightedBrokerScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedBrokerScoreContactEmailLengthRule(var maxLength: Int) : WeightedBrokerScoreRule {
    override fun check(item: WeightedBrokerScore): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted broker score. */
data class WeightedBrokerScoreValidator(var rules: MutableList<WeightedBrokerScoreRule>) {
    fun validate(item: WeightedBrokerScore): MutableList<String> {
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

    fun requireValid(item: WeightedBrokerScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedBrokerScore>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedBrokerScoreValidator(): WeightedBrokerScoreValidator {
    return WeightedBrokerScoreValidator(
        mutableListOf<WeightedBrokerScoreRule>(
            WeightedBrokerScoreRequiredContactEmailRule("weighted broker score needs a value"),
            WeightedBrokerScoreCodeLengthRule(32),
            WeightedBrokerScoreLabelLengthRule(120),
            WeightedBrokerScoreContactEmailLengthRule(1000),
        ),
    )
}
