package com.acme.transport.linehaul.laneplan.planwindow

/** A rule a strict lane plan window must pass. Check returns its problems, or none. */
interface StrictLanePlanWindowRule {
    fun check(item: StrictLanePlanWindow): MutableList<String>
}

data class StrictLanePlanWindowCodePrefixRule(var prefix: String) : StrictLanePlanWindowRule {
    override fun check(item: StrictLanePlanWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictLanePlanWindowRequiredChecksumRule(var message: String) : StrictLanePlanWindowRule {
    override fun check(item: StrictLanePlanWindow): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictLanePlanWindowLimitRangeRule(var min: Int, var max: Int) : StrictLanePlanWindowRule {
    override fun check(item: StrictLanePlanWindow): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class StrictLanePlanWindowRequiredCodeRule(var message: String) : StrictLanePlanWindowRule {
    override fun check(item: StrictLanePlanWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictLanePlanWindowLoadKgRangeRule(var min: Double, var max: Double) : StrictLanePlanWindowRule {
    override fun check(item: StrictLanePlanWindow): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict lane plan window. */
data class StrictLanePlanWindowValidator(var rules: MutableList<StrictLanePlanWindowRule>) {
    fun validate(item: StrictLanePlanWindow): MutableList<String> {
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

    fun requireValid(item: StrictLanePlanWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictLanePlanWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictLanePlanWindowValidator(): StrictLanePlanWindowValidator {
    return StrictLanePlanWindowValidator(
        mutableListOf<StrictLanePlanWindowRule>(
            StrictLanePlanWindowCodePrefixRule("X"),
            StrictLanePlanWindowRequiredChecksumRule("strict lane plan window needs a value"),
            StrictLanePlanWindowLimitRangeRule(64, 3600),
            StrictLanePlanWindowRequiredCodeRule("strict lane plan window needs this field"),
            StrictLanePlanWindowLoadKgRangeRule(2.3, 3685.7),
        ),
    )
}
