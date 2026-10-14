package com.acme.transport.terminal.crossdock.slot

/** A rule a cross dock agreement must pass. Check returns its problems, or none. */
interface CrossDockAgreementRule {
    fun check(item: CrossDockAgreement): MutableList<String>
}

data class CrossDockAgreementGraceDaysRangeRule(var min: Int, var max: Int) : CrossDockAgreementRule {
    override fun check(item: CrossDockAgreement): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockAgreementCapacityRangeRule(var min: Int, var max: Int) : CrossDockAgreementRule {
    override fun check(item: CrossDockAgreement): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockAgreementNameLengthRule(var maxLength: Int) : CrossDockAgreementRule {
    override fun check(item: CrossDockAgreement): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock agreement. */
data class CrossDockAgreementValidator(var rules: MutableList<CrossDockAgreementRule>) {
    fun validate(item: CrossDockAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockAgreementValidator(): CrossDockAgreementValidator {
    return CrossDockAgreementValidator(
        mutableListOf<CrossDockAgreementRule>(
            CrossDockAgreementGraceDaysRangeRule(64, 86400),
            CrossDockAgreementCapacityRangeRule(16, 120),
            CrossDockAgreementNameLengthRule(80),
        ),
    )
}
