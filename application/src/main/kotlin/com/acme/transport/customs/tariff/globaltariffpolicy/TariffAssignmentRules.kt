package com.acme.transport.customs.tariff.globaltariffpolicy

/** A rule a tariff assignment must pass. Check returns its problems, or none. */
interface TariffAssignmentRule {
    fun check(item: TariffAssignment): MutableList<String>
}

data class TariffAssignmentMarginRangeRule(var min: Double, var max: Double) : TariffAssignmentRule {
    override fun check(item: TariffAssignment): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class TariffAssignmentCodeLengthRule(var maxLength: Int) : TariffAssignmentRule {
    override fun check(item: TariffAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffAssignmentNameLengthRule(var maxLength: Int) : TariffAssignmentRule {
    override fun check(item: TariffAssignment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff assignment. */
data class TariffAssignmentValidator(var rules: MutableList<TariffAssignmentRule>) {
    fun validate(item: TariffAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TariffAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffAssignmentValidator(): TariffAssignmentValidator {
    return TariffAssignmentValidator(
        mutableListOf<TariffAssignmentRule>(
            TariffAssignmentMarginRangeRule(6.8, 1405.3),
            TariffAssignmentCodeLengthRule(3600),
            TariffAssignmentNameLengthRule(1000),
        ),
    )
}
