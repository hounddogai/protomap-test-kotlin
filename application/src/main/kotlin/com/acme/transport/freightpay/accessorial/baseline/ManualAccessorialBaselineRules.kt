package com.acme.transport.freightpay.accessorial.baseline

/** A rule a manual accessorial baseline must pass. Check returns its problems, or none. */
interface ManualAccessorialBaselineRule {
    fun check(item: ManualAccessorialBaseline): MutableList<String>
}

data class ManualAccessorialBaselineWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : ManualAccessorialBaselineRule {
    override fun check(item: ManualAccessorialBaseline): MutableList<String> {
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

data class ManualAccessorialBaselineLabelLengthRule(var maxLength: Int) : ManualAccessorialBaselineRule {
    override fun check(item: ManualAccessorialBaseline): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualAccessorialBaselineRequiredLabelRule(var message: String) : ManualAccessorialBaselineRule {
    override fun check(item: ManualAccessorialBaseline): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualAccessorialBaselineRequiredCodeRule(var message: String) : ManualAccessorialBaselineRule {
    override fun check(item: ManualAccessorialBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual accessorial baseline. */
data class ManualAccessorialBaselineValidator(var rules: MutableList<ManualAccessorialBaselineRule>) {
    fun validate(item: ManualAccessorialBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualAccessorialBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualAccessorialBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultManualAccessorialBaselineValidator(): ManualAccessorialBaselineValidator {
    return ManualAccessorialBaselineValidator(
        mutableListOf<ManualAccessorialBaselineRule>(
            ManualAccessorialBaselineWindowDaysRangeRule(255, 255),
            ManualAccessorialBaselineLabelLengthRule(120),
            ManualAccessorialBaselineRequiredLabelRule("manual accessorial baseline needs an entry"),
            ManualAccessorialBaselineRequiredCodeRule("manual accessorial baseline needs a value"),
        ),
    )
}
