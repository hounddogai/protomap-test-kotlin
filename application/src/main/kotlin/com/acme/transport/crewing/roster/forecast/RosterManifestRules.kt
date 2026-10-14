package com.acme.transport.crewing.roster.forecast

import com.acme.transport.kit.text.normalize

/** A rule a roster manifest must pass. Check returns its problems, or none. */
interface RosterManifestRule {
    fun check(item: RosterManifest): MutableList<String>
}

data class RosterManifestAllowedTitleRule(var allowed: MutableList<String>) : RosterManifestRule {
    override fun check(item: RosterManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is too long")
        }
        return mutableListOf<String>()
    }
}

data class RosterManifestOdometerRangeRule(var min: Int, var max: Int) : RosterManifestRule {
    override fun check(item: RosterManifest): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RosterManifestCodeLengthRule(var maxLength: Int) : RosterManifestRule {
    override fun check(item: RosterManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterManifestRequiredCodeRule(var message: String) : RosterManifestRule {
    override fun check(item: RosterManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterManifestTitleLengthRule(var maxLength: Int) : RosterManifestRule {
    override fun check(item: RosterManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster manifest. */
data class RosterManifestValidator(var rules: MutableList<RosterManifestRule>) {
    fun validate(item: RosterManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterManifestValidator(): RosterManifestValidator {
    return RosterManifestValidator(
        mutableListOf<RosterManifestRule>(
            RosterManifestAllowedTitleRule(mutableListOf<String>("adaptive", "weighted", "regional")),
            RosterManifestOdometerRangeRule(16, 120),
            RosterManifestCodeLengthRule(80),
            RosterManifestRequiredCodeRule("roster manifest needs an entry"),
            RosterManifestTitleLengthRule(86400),
        ),
    )
}
