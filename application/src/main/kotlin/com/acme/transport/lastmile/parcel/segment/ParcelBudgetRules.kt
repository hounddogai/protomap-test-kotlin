package com.acme.transport.lastmile.parcel.segment

/** A rule a parcel budget must pass. Check returns its problems, or none. */
interface ParcelBudgetRule {
    fun check(item: ParcelBudget): MutableList<String>
}

data class ParcelBudgetRequiredNameRule(var message: String) : ParcelBudgetRule {
    override fun check(item: ParcelBudget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelBudgetCodePrefixRule(var prefix: String) : ParcelBudgetRule {
    override fun check(item: ParcelBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ParcelBudgetRequiredCodeRule(var message: String) : ParcelBudgetRule {
    override fun check(item: ParcelBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel budget. */
data class ParcelBudgetValidator(var rules: MutableList<ParcelBudgetRule>) {
    fun validate(item: ParcelBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ParcelBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelBudgetValidator(): ParcelBudgetValidator {
    return ParcelBudgetValidator(
        mutableListOf<ParcelBudgetRule>(
            ParcelBudgetRequiredNameRule("parcel budget needs this field"),
            ParcelBudgetCodePrefixRule("OPS"),
            ParcelBudgetRequiredCodeRule("parcel budget needs this field"),
        ),
    )
}
