package com.acme.transport.emissions.idling.proposal

/** A rule a idling allocation must pass. Check returns its problems, or none. */
interface IdlingAllocationRule {
    fun check(item: IdlingAllocation): MutableList<String>
}

data class IdlingAllocationScoreRangeRule(var min: Double, var max: Double) : IdlingAllocationRule {
    override fun check(item: IdlingAllocation): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IdlingAllocationLabelLengthRule(var maxLength: Int) : IdlingAllocationRule {
    override fun check(item: IdlingAllocation): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingAllocationRequiredCountryRule(var message: String) : IdlingAllocationRule {
    override fun check(item: IdlingAllocation): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingAllocationCodeLengthRule(var maxLength: Int) : IdlingAllocationRule {
    override fun check(item: IdlingAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling allocation. */
data class IdlingAllocationValidator(var rules: MutableList<IdlingAllocationRule>) {
    fun validate(item: IdlingAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IdlingAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingAllocationValidator(): IdlingAllocationValidator {
    return IdlingAllocationValidator(
        mutableListOf<IdlingAllocationRule>(
            IdlingAllocationScoreRangeRule(4.0, 2784.5),
            IdlingAllocationLabelLengthRule(1000),
            IdlingAllocationRequiredCountryRule("idling allocation needs a value"),
            IdlingAllocationCodeLengthRule(120),
        ),
    )
}
