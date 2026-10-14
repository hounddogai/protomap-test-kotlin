package com.acme.transport.crewing.restbreak.breakprofile

/** A rule a manual rest break profile must pass. Check returns its problems, or none. */
interface ManualRestBreakProfileRule {
    fun check(item: ManualRestBreakProfile): MutableList<String>
}

data class ManualRestBreakProfileCodePrefixRule(var prefix: String) : ManualRestBreakProfileRule {
    override fun check(item: ManualRestBreakProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualRestBreakProfileStopCountRangeRule(var min: Int, var max: Int) : ManualRestBreakProfileRule {
    override fun check(item: ManualRestBreakProfile): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualRestBreakProfileRequiredPhoneRule(var message: String) : ManualRestBreakProfileRule {
    override fun check(item: ManualRestBreakProfile): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualRestBreakProfileLabelLengthRule(var maxLength: Int) : ManualRestBreakProfileRule {
    override fun check(item: ManualRestBreakProfile): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualRestBreakProfileRequiredCodeRule(var message: String) : ManualRestBreakProfileRule {
    override fun check(item: ManualRestBreakProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual rest break profile. */
data class ManualRestBreakProfileValidator(var rules: MutableList<ManualRestBreakProfileRule>) {
    fun validate(item: ManualRestBreakProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualRestBreakProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualRestBreakProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultManualRestBreakProfileValidator(): ManualRestBreakProfileValidator {
    return ManualRestBreakProfileValidator(
        mutableListOf<ManualRestBreakProfileRule>(
            ManualRestBreakProfileCodePrefixRule("X"),
            ManualRestBreakProfileStopCountRangeRule(16, 255),
            ManualRestBreakProfileRequiredPhoneRule("manual rest break profile needs an entry"),
            ManualRestBreakProfileLabelLengthRule(1000),
            ManualRestBreakProfileRequiredCodeRule("manual rest break profile needs a value"),
        ),
    )
}
