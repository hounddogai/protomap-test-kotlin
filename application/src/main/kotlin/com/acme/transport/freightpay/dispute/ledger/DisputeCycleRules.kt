package com.acme.transport.freightpay.dispute.ledger

/** A rule a dispute cycle must pass. Check returns its problems, or none. */
interface DisputeCycleRule {
    fun check(item: DisputeCycle): MutableList<String>
}

data class DisputeCycleRequiredCategoryRule(var message: String) : DisputeCycleRule {
    override fun check(item: DisputeCycle): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeCycleCategoryLengthRule(var maxLength: Int) : DisputeCycleRule {
    override fun check(item: DisputeCycle): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeCycleNameLengthRule(var maxLength: Int) : DisputeCycleRule {
    override fun check(item: DisputeCycle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeCycleCodeLengthRule(var maxLength: Int) : DisputeCycleRule {
    override fun check(item: DisputeCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute cycle. */
data class DisputeCycleValidator(var rules: MutableList<DisputeCycleRule>) {
    fun validate(item: DisputeCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DisputeCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeCycleValidator(): DisputeCycleValidator {
    return DisputeCycleValidator(
        mutableListOf<DisputeCycleRule>(
            DisputeCycleRequiredCategoryRule("dispute cycle needs an entry"),
            DisputeCycleCategoryLengthRule(80),
            DisputeCycleNameLengthRule(255),
            DisputeCycleCodeLengthRule(255),
        ),
    )
}
