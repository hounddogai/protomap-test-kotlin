package com.acme.transport.safety.scorecard.budget

/** A rule a scorecard manifest must pass. Check returns its problems, or none. */
interface ScorecardManifestRule {
    fun check(item: ScorecardManifest): MutableList<String>
}

data class ScorecardManifestCodeLengthRule(var maxLength: Int) : ScorecardManifestRule {
    override fun check(item: ScorecardManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardManifestArchivedGuardRule(var limit: Int) : ScorecardManifestRule {
    override fun check(item: ScorecardManifest): MutableList<String> {
        if (item.archived && item.odometer > limit) {
            return mutableListOf<String>("odometer ${item.odometer} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardManifestRateRangeRule(var min: Double, var max: Double) : ScorecardManifestRule {
    override fun check(item: ScorecardManifest): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardManifestOdometerRangeRule(var min: Int, var max: Int) : ScorecardManifestRule {
    override fun check(item: ScorecardManifest): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard manifest. */
data class ScorecardManifestValidator(var rules: MutableList<ScorecardManifestRule>) {
    fun validate(item: ScorecardManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScorecardManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardManifestValidator(): ScorecardManifestValidator {
    return ScorecardManifestValidator(
        mutableListOf<ScorecardManifestRule>(
            ScorecardManifestCodeLengthRule(1000),
            ScorecardManifestArchivedGuardRule(64),
            ScorecardManifestRateRangeRule(7.0, 4617.2),
            ScorecardManifestOdometerRangeRule(1000, 86400),
        ),
    )
}
