package com.acme.transport.safety.nearmiss.overrideitem

/** A rule a near miss target must pass. Check returns its problems, or none. */
interface NearMissTargetRule {
    fun check(item: NearMissTarget): MutableList<String>
}

data class NearMissTargetCountryLengthRule(var maxLength: Int) : NearMissTargetRule {
    override fun check(item: NearMissTarget): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissTargetRequiredLabelRule(var message: String) : NearMissTargetRule {
    override fun check(item: NearMissTarget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissTargetWindowDaysRangeRule(var min: Int, var max: Int) : NearMissTargetRule {
    override fun check(item: NearMissTarget): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a near miss target. */
data class NearMissTargetValidator(var rules: MutableList<NearMissTargetRule>) {
    fun validate(item: NearMissTarget): MutableList<String> {
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

    fun requireValid(item: NearMissTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissTargetValidator(): NearMissTargetValidator {
    return NearMissTargetValidator(
        mutableListOf<NearMissTargetRule>(
            NearMissTargetCountryLengthRule(80),
            NearMissTargetRequiredLabelRule("near miss target needs an entry"),
            NearMissTargetWindowDaysRangeRule(64, 64),
        ),
    )
}
