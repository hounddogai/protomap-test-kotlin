package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.kit.text.normalize

/** A rule a rolling roster rollup must pass. Check returns its problems, or none. */
interface RollingRosterRollupRule {
    fun check(item: RollingRosterRollup): MutableList<String>
}

data class RollingRosterRollupCodePrefixRule(var prefix: String) : RollingRosterRollupRule {
    override fun check(item: RollingRosterRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingRosterRollupRequiredCountryRule(var message: String) : RollingRosterRollupRule {
    override fun check(item: RollingRosterRollup): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingRosterRollupCarrierLengthRule(var maxLength: Int) : RollingRosterRollupRule {
    override fun check(item: RollingRosterRollup): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingRosterRollupAllowedLabelRule(var allowed: MutableList<String>) : RollingRosterRollupRule {
    override fun check(item: RollingRosterRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling roster rollup. */
data class RollingRosterRollupValidator(var rules: MutableList<RollingRosterRollupRule>) {
    fun validate(item: RollingRosterRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingRosterRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingRosterRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingRosterRollupValidator(): RollingRosterRollupValidator {
    return RollingRosterRollupValidator(
        mutableListOf<RollingRosterRollupRule>(
            RollingRosterRollupCodePrefixRule("OPS"),
            RollingRosterRollupRequiredCountryRule("rolling roster rollup needs this field"),
            RollingRosterRollupCarrierLengthRule(120),
            RollingRosterRollupAllowedLabelRule(mutableListOf<String>()),
        ),
    )
}
