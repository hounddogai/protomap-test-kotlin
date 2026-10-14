package com.acme.transport.linehaul.capacity.audit

/** A rule a capacity agreement must pass. Check returns its problems, or none. */
interface CapacityAgreementRule {
    fun check(item: CapacityAgreement): MutableList<String>
}

data class CapacityAgreementRequiredTitleRule(var message: String) : CapacityAgreementRule {
    override fun check(item: CapacityAgreement): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityAgreementPhoneLengthRule(var maxLength: Int) : CapacityAgreementRule {
    override fun check(item: CapacityAgreement): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityAgreementTitleLengthRule(var maxLength: Int) : CapacityAgreementRule {
    override fun check(item: CapacityAgreement): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityAgreementRequiredCodeRule(var message: String) : CapacityAgreementRule {
    override fun check(item: CapacityAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityAgreementMaximumRangeRule(var min: Double, var max: Double) : CapacityAgreementRule {
    override fun check(item: CapacityAgreement): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capacity agreement. */
data class CapacityAgreementValidator(var rules: MutableList<CapacityAgreementRule>) {
    fun validate(item: CapacityAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacityAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityAgreementValidator(): CapacityAgreementValidator {
    return CapacityAgreementValidator(
        mutableListOf<CapacityAgreementRule>(
            CapacityAgreementRequiredTitleRule("capacity agreement needs a value"),
            CapacityAgreementPhoneLengthRule(32),
            CapacityAgreementTitleLengthRule(86400),
            CapacityAgreementRequiredCodeRule("capacity agreement needs a value"),
            CapacityAgreementMaximumRangeRule(7.9, 4501.5),
        ),
    )
}
