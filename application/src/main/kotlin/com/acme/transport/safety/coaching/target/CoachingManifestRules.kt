package com.acme.transport.safety.coaching.target

/** A rule a coaching manifest must pass. Check returns its problems, or none. */
interface CoachingManifestRule {
    fun check(item: CoachingManifest): MutableList<String>
}

data class CoachingManifestThresholdRangeRule(var min: Double, var max: Double) : CoachingManifestRule {
    override fun check(item: CoachingManifest): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CoachingManifestFuelLitersRangeRule(var min: Double, var max: Double) : CoachingManifestRule {
    override fun check(item: CoachingManifest): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CoachingManifestRequiredNotesRule(var message: String) : CoachingManifestRule {
    override fun check(item: CoachingManifest): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingManifestRequiredCodeRule(var message: String) : CoachingManifestRule {
    override fun check(item: CoachingManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching manifest. */
data class CoachingManifestValidator(var rules: MutableList<CoachingManifestRule>) {
    fun validate(item: CoachingManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CoachingManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingManifestValidator(): CoachingManifestValidator {
    return CoachingManifestValidator(
        mutableListOf<CoachingManifestRule>(
            CoachingManifestThresholdRangeRule(4.1, 2359.4),
            CoachingManifestFuelLitersRangeRule(4.9, 1935.1),
            CoachingManifestRequiredNotesRule("coaching manifest needs this field"),
            CoachingManifestRequiredCodeRule("coaching manifest needs this field"),
        ),
    )
}
