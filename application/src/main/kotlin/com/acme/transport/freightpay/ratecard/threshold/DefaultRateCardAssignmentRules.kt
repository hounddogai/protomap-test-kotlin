package com.acme.transport.freightpay.ratecard.threshold

/** A rule a default rate card assignment must pass. Check returns its problems, or none. */
interface DefaultRateCardAssignmentRule {
    fun check(item: DefaultRateCardAssignment): MutableList<String>
}

data class DefaultRateCardAssignmentCodeLengthRule(var maxLength: Int) : DefaultRateCardAssignmentRule {
    override fun check(item: DefaultRateCardAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRateCardAssignmentRequiredCodeRule(var message: String) : DefaultRateCardAssignmentRule {
    override fun check(item: DefaultRateCardAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRateCardAssignmentRequiredSourceRule(var message: String) : DefaultRateCardAssignmentRule {
    override fun check(item: DefaultRateCardAssignment): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRateCardAssignmentRequiredContactEmailRule(
    var message: String,
) : DefaultRateCardAssignmentRule {
    override fun check(item: DefaultRateCardAssignment): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRateCardAssignmentContactEmailLengthRule(var maxLength: Int) : DefaultRateCardAssignmentRule {
    override fun check(item: DefaultRateCardAssignment): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default rate card assignment. */
data class DefaultRateCardAssignmentValidator(var rules: MutableList<DefaultRateCardAssignmentRule>) {
    fun validate(item: DefaultRateCardAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultRateCardAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRateCardAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRateCardAssignmentValidator(): DefaultRateCardAssignmentValidator {
    return DefaultRateCardAssignmentValidator(
        mutableListOf<DefaultRateCardAssignmentRule>(
            DefaultRateCardAssignmentCodeLengthRule(80),
            DefaultRateCardAssignmentRequiredCodeRule("default rate card assignment needs this field"),
            DefaultRateCardAssignmentRequiredSourceRule("default rate card assignment needs an entry"),
            DefaultRateCardAssignmentRequiredContactEmailRule("default rate card assignment needs this field"),
            DefaultRateCardAssignmentContactEmailLengthRule(64),
        ),
    )
}
