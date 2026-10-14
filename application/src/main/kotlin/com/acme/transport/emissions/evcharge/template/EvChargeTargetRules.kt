package com.acme.transport.emissions.evcharge.template

/** A rule a ev charge target must pass. Check returns its problems, or none. */
interface EvChargeTargetRule {
    fun check(item: EvChargeTarget): MutableList<String>
}

data class EvChargeTargetCodePrefixRule(var prefix: String) : EvChargeTargetRule {
    override fun check(item: EvChargeTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeTargetRequiredTimezoneRule(var message: String) : EvChargeTargetRule {
    override fun check(item: EvChargeTarget): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeTargetTimezoneLengthRule(var maxLength: Int) : EvChargeTargetRule {
    override fun check(item: EvChargeTarget): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge target. */
data class EvChargeTargetValidator(var rules: MutableList<EvChargeTargetRule>) {
    fun validate(item: EvChargeTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EvChargeTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeTargetValidator(): EvChargeTargetValidator {
    return EvChargeTargetValidator(
        mutableListOf<EvChargeTargetRule>(
            EvChargeTargetCodePrefixRule("OPS"),
            EvChargeTargetRequiredTimezoneRule("ev charge target needs an entry"),
            EvChargeTargetTimezoneLengthRule(32),
        ),
    )
}
