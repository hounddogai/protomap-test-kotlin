package com.acme.transport.freightpay.ratecard.cardsegment

/** A rule a default rate card cycle must pass. Check returns its problems, or none. */
interface DefaultRateCardCycleRule {
    fun check(item: DefaultRateCardCycle): MutableList<String>
}

data class DefaultRateCardCycleNameLengthRule(var maxLength: Int) : DefaultRateCardCycleRule {
    override fun check(item: DefaultRateCardCycle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRateCardCycleCodeLengthRule(var maxLength: Int) : DefaultRateCardCycleRule {
    override fun check(item: DefaultRateCardCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRateCardCycleContactEmailLengthRule(var maxLength: Int) : DefaultRateCardCycleRule {
    override fun check(item: DefaultRateCardCycle): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRateCardCycleCodePrefixRule(var prefix: String) : DefaultRateCardCycleRule {
    override fun check(item: DefaultRateCardCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRateCardCycleRequiredContactEmailRule(var message: String) : DefaultRateCardCycleRule {
    override fun check(item: DefaultRateCardCycle): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default rate card cycle. */
data class DefaultRateCardCycleValidator(var rules: MutableList<DefaultRateCardCycleRule>) {
    fun validate(item: DefaultRateCardCycle): MutableList<String> {
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

    fun requireValid(item: DefaultRateCardCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRateCardCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRateCardCycleValidator(): DefaultRateCardCycleValidator {
    return DefaultRateCardCycleValidator(
        mutableListOf<DefaultRateCardCycleRule>(
            DefaultRateCardCycleNameLengthRule(120),
            DefaultRateCardCycleCodeLengthRule(32),
            DefaultRateCardCycleContactEmailLengthRule(255),
            DefaultRateCardCycleCodePrefixRule("REF"),
            DefaultRateCardCycleRequiredContactEmailRule("default rate card cycle needs an entry"),
        ),
    )
}
