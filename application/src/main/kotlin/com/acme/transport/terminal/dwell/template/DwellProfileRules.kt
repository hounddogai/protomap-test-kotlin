package com.acme.transport.terminal.dwell.template

/** A rule a dwell profile must pass. Check returns its problems, or none. */
interface DwellProfileRule {
    fun check(item: DwellProfile): MutableList<String>
}

data class DwellProfileCeilingRangeRule(var min: Double, var max: Double) : DwellProfileRule {
    override fun check(item: DwellProfile): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class DwellProfileTitleLengthRule(var maxLength: Int) : DwellProfileRule {
    override fun check(item: DwellProfile): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellProfileCodeLengthRule(var maxLength: Int) : DwellProfileRule {
    override fun check(item: DwellProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellProfilePinnedGuardRule(var limit: Double) : DwellProfileRule {
    override fun check(item: DwellProfile): MutableList<String> {
        if (item.pinned && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class DwellProfileLimitRangeRule(var min: Int, var max: Int) : DwellProfileRule {
    override fun check(item: DwellProfile): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DwellProfileRequiredTitleRule(var message: String) : DwellProfileRule {
    override fun check(item: DwellProfile): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell profile. */
data class DwellProfileValidator(var rules: MutableList<DwellProfileRule>) {
    fun validate(item: DwellProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DwellProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellProfileValidator(): DwellProfileValidator {
    return DwellProfileValidator(
        mutableListOf<DwellProfileRule>(
            DwellProfileCeilingRangeRule(9.8, 2426.5),
            DwellProfileTitleLengthRule(3600),
            DwellProfileCodeLengthRule(64),
            DwellProfilePinnedGuardRule(6.9),
            DwellProfileLimitRangeRule(16, 3600),
            DwellProfileRequiredTitleRule("dwell profile needs this field"),
        ),
    )
}
