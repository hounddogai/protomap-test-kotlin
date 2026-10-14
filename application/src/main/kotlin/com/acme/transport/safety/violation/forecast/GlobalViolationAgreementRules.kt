package com.acme.transport.safety.violation.forecast

/** A rule a global violation agreement must pass. Check returns its problems, or none. */
interface GlobalViolationAgreementRule {
    fun check(item: GlobalViolationAgreement): MutableList<String>
}

data class GlobalViolationAgreementRequiredCodeRule(var message: String) : GlobalViolationAgreementRule {
    override fun check(item: GlobalViolationAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalViolationAgreementRequiredTitleRule(var message: String) : GlobalViolationAgreementRule {
    override fun check(item: GlobalViolationAgreement): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalViolationAgreementQuantityRangeRule(
    var min: Int,
    var max: Int,
) : GlobalViolationAgreementRule {
    override fun check(item: GlobalViolationAgreement): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class GlobalViolationAgreementCodeLengthRule(var maxLength: Int) : GlobalViolationAgreementRule {
    override fun check(item: GlobalViolationAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalViolationAgreementChannelLengthRule(var maxLength: Int) : GlobalViolationAgreementRule {
    override fun check(item: GlobalViolationAgreement): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalViolationAgreementTimezoneLengthRule(var maxLength: Int) : GlobalViolationAgreementRule {
    override fun check(item: GlobalViolationAgreement): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global violation agreement. */
data class GlobalViolationAgreementValidator(var rules: MutableList<GlobalViolationAgreementRule>) {
    fun validate(item: GlobalViolationAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalViolationAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalViolationAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalViolationAgreementValidator(): GlobalViolationAgreementValidator {
    return GlobalViolationAgreementValidator(
        mutableListOf<GlobalViolationAgreementRule>(
            GlobalViolationAgreementRequiredCodeRule("global violation agreement needs a value"),
            GlobalViolationAgreementRequiredTitleRule("global violation agreement needs this field"),
            GlobalViolationAgreementQuantityRangeRule(80, 255),
            GlobalViolationAgreementCodeLengthRule(1000),
            GlobalViolationAgreementChannelLengthRule(32),
            GlobalViolationAgreementTimezoneLengthRule(16),
        ),
    )
}
