package com.acme.transport.linehaul.relay.rule

/** A rule a relay template must pass. Check returns its problems, or none. */
interface RelayTemplateRule {
    fun check(item: RelayTemplate): MutableList<String>
}

data class RelayTemplateRequiredDriverIdRule(var message: String) : RelayTemplateRule {
    override fun check(item: RelayTemplate): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayTemplateCodeLengthRule(var maxLength: Int) : RelayTemplateRule {
    override fun check(item: RelayTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayTemplateRequiredLabelRule(var message: String) : RelayTemplateRule {
    override fun check(item: RelayTemplate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay template. */
data class RelayTemplateValidator(var rules: MutableList<RelayTemplateRule>) {
    fun validate(item: RelayTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RelayTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayTemplateValidator(): RelayTemplateValidator {
    return RelayTemplateValidator(
        mutableListOf<RelayTemplateRule>(
            RelayTemplateRequiredDriverIdRule("relay template needs a value"),
            RelayTemplateCodeLengthRule(255),
            RelayTemplateRequiredLabelRule("relay template needs this field"),
        ),
    )
}
