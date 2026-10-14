package com.acme.transport.crewing.qualification.qualificationpreference

/** A rule a manual qualification profile must pass. Check returns its problems, or none. */
interface ManualQualificationProfileRule {
    fun check(item: ManualQualificationProfile): MutableList<String>
}

data class ManualQualificationProfileCodePrefixRule(var prefix: String) : ManualQualificationProfileRule {
    override fun check(item: ManualQualificationProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualQualificationProfileRequiredCodeRule(var message: String) : ManualQualificationProfileRule {
    override fun check(item: ManualQualificationProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualQualificationProfileLabelLengthRule(var maxLength: Int) : ManualQualificationProfileRule {
    override fun check(item: ManualQualificationProfile): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualQualificationProfileRequiredTitleRule(var message: String) : ManualQualificationProfileRule {
    override fun check(item: ManualQualificationProfile): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualQualificationProfileStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : ManualQualificationProfileRule {
    override fun check(item: ManualQualificationProfile): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class ManualQualificationProfileStopCountRangeRule(
    var min: Int,
    var max: Int,
) : ManualQualificationProfileRule {
    override fun check(item: ManualQualificationProfile): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a manual qualification profile. */
data class ManualQualificationProfileValidator(var rules: MutableList<ManualQualificationProfileRule>) {
    fun validate(item: ManualQualificationProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualQualificationProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualQualificationProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultManualQualificationProfileValidator(): ManualQualificationProfileValidator {
    return ManualQualificationProfileValidator(
        mutableListOf<ManualQualificationProfileRule>(
            ManualQualificationProfileCodePrefixRule("OPS"),
            ManualQualificationProfileRequiredCodeRule("manual qualification profile needs a value"),
            ManualQualificationProfileLabelLengthRule(3600),
            ManualQualificationProfileRequiredTitleRule("manual qualification profile needs an entry"),
            ManualQualificationProfileStartsAtRangeRule(80, 86400),
            ManualQualificationProfileStopCountRangeRule(16, 32),
        ),
    )
}
