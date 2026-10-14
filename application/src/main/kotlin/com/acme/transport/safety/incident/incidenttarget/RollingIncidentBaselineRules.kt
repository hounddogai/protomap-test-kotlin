package com.acme.transport.safety.incident.incidenttarget

/** A rule a rolling incident baseline must pass. Check returns its problems, or none. */
interface RollingIncidentBaselineRule {
    fun check(item: RollingIncidentBaseline): MutableList<String>
}

data class RollingIncidentBaselineTitleLengthRule(var maxLength: Int) : RollingIncidentBaselineRule {
    override fun check(item: RollingIncidentBaseline): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingIncidentBaselineRequiredTitleRule(var message: String) : RollingIncidentBaselineRule {
    override fun check(item: RollingIncidentBaseline): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingIncidentBaselineRequiredCodeRule(var message: String) : RollingIncidentBaselineRule {
    override fun check(item: RollingIncidentBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingIncidentBaselineNameLengthRule(var maxLength: Int) : RollingIncidentBaselineRule {
    override fun check(item: RollingIncidentBaseline): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingIncidentBaselineWindowRule(var maxSpan: Int) : RollingIncidentBaselineRule {
    override fun check(item: RollingIncidentBaseline): MutableList<String> {
        if (item.startsAt > item.endsAt) {
            return mutableListOf<String>("starts at is after ends at")
        }
        if (maxSpan > 0 && item.endsAt - item.startsAt > maxSpan) {
            return mutableListOf<String>("spans more than $maxSpan seconds")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling incident baseline. */
data class RollingIncidentBaselineValidator(var rules: MutableList<RollingIncidentBaselineRule>) {
    fun validate(item: RollingIncidentBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingIncidentBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingIncidentBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingIncidentBaselineValidator(): RollingIncidentBaselineValidator {
    return RollingIncidentBaselineValidator(
        mutableListOf<RollingIncidentBaselineRule>(
            RollingIncidentBaselineTitleLengthRule(86400),
            RollingIncidentBaselineRequiredTitleRule("rolling incident baseline needs an entry"),
            RollingIncidentBaselineRequiredCodeRule("rolling incident baseline needs this field"),
            RollingIncidentBaselineNameLengthRule(3600),
            RollingIncidentBaselineWindowRule(32),
        ),
    )
}
