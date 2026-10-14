package com.acme.transport.freightpay.ratecard.entry

/** A rule a default rate card entry must pass. Check returns its problems, or none. */
interface DefaultRateCardEntryRule {
    fun check(item: DefaultRateCardEntry): MutableList<String>
}

data class DefaultRateCardEntryRequiredCategoryRule(var message: String) : DefaultRateCardEntryRule {
    override fun check(item: DefaultRateCardEntry): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRateCardEntryRouteCodeLengthRule(var maxLength: Int) : DefaultRateCardEntryRule {
    override fun check(item: DefaultRateCardEntry): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRateCardEntryRequiredRouteCodeRule(var message: String) : DefaultRateCardEntryRule {
    override fun check(item: DefaultRateCardEntry): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRateCardEntryRevisionRangeRule(var min: Int, var max: Int) : DefaultRateCardEntryRule {
    override fun check(item: DefaultRateCardEntry): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRateCardEntryTitleLengthRule(var maxLength: Int) : DefaultRateCardEntryRule {
    override fun check(item: DefaultRateCardEntry): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default rate card entry. */
data class DefaultRateCardEntryValidator(var rules: MutableList<DefaultRateCardEntryRule>) {
    fun validate(item: DefaultRateCardEntry): MutableList<String> {
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

    fun requireValid(item: DefaultRateCardEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRateCardEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRateCardEntryValidator(): DefaultRateCardEntryValidator {
    return DefaultRateCardEntryValidator(
        mutableListOf<DefaultRateCardEntryRule>(
            DefaultRateCardEntryRequiredCategoryRule("default rate card entry needs a value"),
            DefaultRateCardEntryRouteCodeLengthRule(86400),
            DefaultRateCardEntryRequiredRouteCodeRule("default rate card entry needs a value"),
            DefaultRateCardEntryRevisionRangeRule(16, 255),
            DefaultRateCardEntryTitleLengthRule(3600),
        ),
    )
}
