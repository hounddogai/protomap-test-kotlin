package com.acme.transport.linehaul.relay.relayrule

/** A rule a regional relay baseline must pass. Check returns its problems, or none. */
interface RegionalRelayBaselineRule {
    fun check(item: RegionalRelayBaseline): MutableList<String>
}

data class RegionalRelayBaselineRequiredTimezoneRule(var message: String) : RegionalRelayBaselineRule {
    override fun check(item: RegionalRelayBaseline): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalRelayBaselineTimezoneLengthRule(var maxLength: Int) : RegionalRelayBaselineRule {
    override fun check(item: RegionalRelayBaseline): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRelayBaselineLabelLengthRule(var maxLength: Int) : RegionalRelayBaselineRule {
    override fun check(item: RegionalRelayBaseline): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional relay baseline. */
data class RegionalRelayBaselineValidator(var rules: MutableList<RegionalRelayBaselineRule>) {
    fun validate(item: RegionalRelayBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalRelayBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalRelayBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalRelayBaselineValidator(): RegionalRelayBaselineValidator {
    return RegionalRelayBaselineValidator(
        mutableListOf<RegionalRelayBaselineRule>(
            RegionalRelayBaselineRequiredTimezoneRule("regional relay baseline needs this field"),
            RegionalRelayBaselineTimezoneLengthRule(3600),
            RegionalRelayBaselineLabelLengthRule(120),
        ),
    )
}
