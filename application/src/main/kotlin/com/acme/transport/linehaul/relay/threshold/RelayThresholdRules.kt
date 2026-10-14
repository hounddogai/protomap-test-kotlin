package com.acme.transport.linehaul.relay.threshold

/** A rule a relay threshold must pass. Check returns its problems, or none. */
interface RelayThresholdRule {
    fun check(item: RelayThreshold): MutableList<String>
}

data class RelayThresholdDescriptionLengthRule(var maxLength: Int) : RelayThresholdRule {
    override fun check(item: RelayThreshold): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayThresholdRequiredTitleRule(var message: String) : RelayThresholdRule {
    override fun check(item: RelayThreshold): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayThresholdRequiredDescriptionRule(var message: String) : RelayThresholdRule {
    override fun check(item: RelayThreshold): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayThresholdCodeLengthRule(var maxLength: Int) : RelayThresholdRule {
    override fun check(item: RelayThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay threshold. */
data class RelayThresholdValidator(var rules: MutableList<RelayThresholdRule>) {
    fun validate(item: RelayThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RelayThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayThresholdValidator(): RelayThresholdValidator {
    return RelayThresholdValidator(
        mutableListOf<RelayThresholdRule>(
            RelayThresholdDescriptionLengthRule(32),
            RelayThresholdRequiredTitleRule("relay threshold needs a value"),
            RelayThresholdRequiredDescriptionRule("relay threshold needs a value"),
            RelayThresholdCodeLengthRule(3600),
        ),
    )
}
