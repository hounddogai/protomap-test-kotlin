package com.acme.transport.emissions.idling.idlingtarget

/** A rule a pending idling profile must pass. Check returns its problems, or none. */
interface PendingIdlingProfileRule {
    fun check(item: PendingIdlingProfile): MutableList<String>
}

data class PendingIdlingProfileRequiredCodeRule(var message: String) : PendingIdlingProfileRule {
    override fun check(item: PendingIdlingProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingIdlingProfileCodePrefixRule(var prefix: String) : PendingIdlingProfileRule {
    override fun check(item: PendingIdlingProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingIdlingProfileRequiredDescriptionRule(var message: String) : PendingIdlingProfileRule {
    override fun check(item: PendingIdlingProfile): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingIdlingProfileAmountRangeRule(var min: Double, var max: Double) : PendingIdlingProfileRule {
    override fun check(item: PendingIdlingProfile): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class PendingIdlingProfileRequiredNameRule(var message: String) : PendingIdlingProfileRule {
    override fun check(item: PendingIdlingProfile): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingIdlingProfileNameLengthRule(var maxLength: Int) : PendingIdlingProfileRule {
    override fun check(item: PendingIdlingProfile): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending idling profile. */
data class PendingIdlingProfileValidator(var rules: MutableList<PendingIdlingProfileRule>) {
    fun validate(item: PendingIdlingProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingIdlingProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingIdlingProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingIdlingProfileValidator(): PendingIdlingProfileValidator {
    return PendingIdlingProfileValidator(
        mutableListOf<PendingIdlingProfileRule>(
            PendingIdlingProfileRequiredCodeRule("pending idling profile needs a value"),
            PendingIdlingProfileCodePrefixRule("Q"),
            PendingIdlingProfileRequiredDescriptionRule("pending idling profile needs an entry"),
            PendingIdlingProfileAmountRangeRule(2.0, 2115.7),
            PendingIdlingProfileRequiredNameRule("pending idling profile needs this field"),
            PendingIdlingProfileNameLengthRule(64),
        ),
    )
}
