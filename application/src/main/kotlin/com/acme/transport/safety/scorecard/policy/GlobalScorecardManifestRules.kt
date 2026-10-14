package com.acme.transport.safety.scorecard.policy

/** A rule a global scorecard manifest must pass. Check returns its problems, or none. */
interface GlobalScorecardManifestRule {
    fun check(item: GlobalScorecardManifest): MutableList<String>
}

data class GlobalScorecardManifestCodeLengthRule(var maxLength: Int) : GlobalScorecardManifestRule {
    override fun check(item: GlobalScorecardManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalScorecardManifestRequiredNameRule(var message: String) : GlobalScorecardManifestRule {
    override fun check(item: GlobalScorecardManifest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalScorecardManifestStartsAtRangeRule(var min: Int, var max: Int) : GlobalScorecardManifestRule {
    override fun check(item: GlobalScorecardManifest): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalScorecardManifestDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : GlobalScorecardManifestRule {
    override fun check(item: GlobalScorecardManifest): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class GlobalScorecardManifestOdometerRangeRule(var min: Int, var max: Int) : GlobalScorecardManifestRule {
    override fun check(item: GlobalScorecardManifest): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class GlobalScorecardManifestRequiredDescriptionRule(var message: String) : GlobalScorecardManifestRule {
    override fun check(item: GlobalScorecardManifest): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global scorecard manifest. */
data class GlobalScorecardManifestValidator(var rules: MutableList<GlobalScorecardManifestRule>) {
    fun validate(item: GlobalScorecardManifest): MutableList<String> {
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

    fun requireValid(item: GlobalScorecardManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalScorecardManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalScorecardManifestValidator(): GlobalScorecardManifestValidator {
    return GlobalScorecardManifestValidator(
        mutableListOf<GlobalScorecardManifestRule>(
            GlobalScorecardManifestCodeLengthRule(32),
            GlobalScorecardManifestRequiredNameRule("global scorecard manifest needs an entry"),
            GlobalScorecardManifestStartsAtRangeRule(64, 120),
            GlobalScorecardManifestDurationMinutesRangeRule(120, 255),
            GlobalScorecardManifestOdometerRangeRule(16, 255),
            GlobalScorecardManifestRequiredDescriptionRule("global scorecard manifest needs a value"),
        ),
    )
}
