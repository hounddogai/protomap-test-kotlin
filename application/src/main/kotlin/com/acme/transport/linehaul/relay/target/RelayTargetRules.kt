package com.acme.transport.linehaul.relay.target

/** A rule a relay target must pass. Check returns its problems, or none. */
interface RelayTargetRule {
    fun check(item: RelayTarget): MutableList<String>
}

data class RelayTargetTitleLengthRule(var maxLength: Int) : RelayTargetRule {
    override fun check(item: RelayTarget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayTargetSequenceRangeRule(var min: Int, var max: Int) : RelayTargetRule {
    override fun check(item: RelayTarget): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RelayTargetCodePrefixRule(var prefix: String) : RelayTargetRule {
    override fun check(item: RelayTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RelayTargetRequiredLabelRule(var message: String) : RelayTargetRule {
    override fun check(item: RelayTarget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayTargetRequiredCodeRule(var message: String) : RelayTargetRule {
    override fun check(item: RelayTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay target. */
data class RelayTargetValidator(var rules: MutableList<RelayTargetRule>) {
    fun validate(item: RelayTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RelayTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayTargetValidator(): RelayTargetValidator {
    return RelayTargetValidator(
        mutableListOf<RelayTargetRule>(
            RelayTargetTitleLengthRule(32),
            RelayTargetSequenceRangeRule(255, 1000),
            RelayTargetCodePrefixRule("X"),
            RelayTargetRequiredLabelRule("relay target needs an entry"),
            RelayTargetRequiredCodeRule("relay target needs an entry"),
        ),
    )
}
