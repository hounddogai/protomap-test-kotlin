package com.acme.transport.freightpay.accessorial.allocation

/** A rule a accessorial baseline must pass. Check returns its problems, or none. */
interface AccessorialBaselineRule {
    fun check(item: AccessorialBaseline): MutableList<String>
}

data class AccessorialBaselineRevisionRangeRule(var min: Int, var max: Int) : AccessorialBaselineRule {
    override fun check(item: AccessorialBaseline): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialBaselineTitleLengthRule(var maxLength: Int) : AccessorialBaselineRule {
    override fun check(item: AccessorialBaseline): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialBaselineCountryLengthRule(var maxLength: Int) : AccessorialBaselineRule {
    override fun check(item: AccessorialBaseline): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialBaselineRequiredTitleRule(var message: String) : AccessorialBaselineRule {
    override fun check(item: AccessorialBaseline): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialBaselineRequiredNameRule(var message: String) : AccessorialBaselineRule {
    override fun check(item: AccessorialBaseline): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial baseline. */
data class AccessorialBaselineValidator(var rules: MutableList<AccessorialBaselineRule>) {
    fun validate(item: AccessorialBaseline): MutableList<String> {
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

    fun requireValid(item: AccessorialBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialBaselineValidator(): AccessorialBaselineValidator {
    return AccessorialBaselineValidator(
        mutableListOf<AccessorialBaselineRule>(
            AccessorialBaselineRevisionRangeRule(64, 1000),
            AccessorialBaselineTitleLengthRule(16),
            AccessorialBaselineCountryLengthRule(80),
            AccessorialBaselineRequiredTitleRule("accessorial baseline needs this field"),
            AccessorialBaselineRequiredNameRule("accessorial baseline needs an entry"),
        ),
    )
}
