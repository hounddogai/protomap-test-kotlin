package com.acme.transport.crewing.roster.summary

/** A rule a roster bundle must pass. Check returns its problems, or none. */
interface RosterBundleRule {
    fun check(item: RosterBundle): MutableList<String>
}

data class RosterBundleRequiredCodeRule(var message: String) : RosterBundleRule {
    override fun check(item: RosterBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterBundleRequiredNameRule(var message: String) : RosterBundleRule {
    override fun check(item: RosterBundle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterBundleRequiredUnitRule(var message: String) : RosterBundleRule {
    override fun check(item: RosterBundle): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterBundleUnitLengthRule(var maxLength: Int) : RosterBundleRule {
    override fun check(item: RosterBundle): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster bundle. */
data class RosterBundleValidator(var rules: MutableList<RosterBundleRule>) {
    fun validate(item: RosterBundle): MutableList<String> {
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

    fun requireValid(item: RosterBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterBundleValidator(): RosterBundleValidator {
    return RosterBundleValidator(
        mutableListOf<RosterBundleRule>(
            RosterBundleRequiredCodeRule("roster bundle needs this field"),
            RosterBundleRequiredNameRule("roster bundle needs a value"),
            RosterBundleRequiredUnitRule("roster bundle needs this field"),
            RosterBundleUnitLengthRule(120),
        ),
    )
}
