package com.acme.transport.linehaul.capacity.checkpoint

/** A rule a capacity draft must pass. Check returns its problems, or none. */
interface CapacityDraftRule {
    fun check(item: CapacityDraft): MutableList<String>
}

data class CapacityDraftRequiredTitleRule(var message: String) : CapacityDraftRule {
    override fun check(item: CapacityDraft): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityDraftRequiredUnitRule(var message: String) : CapacityDraftRule {
    override fun check(item: CapacityDraft): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityDraftUnitLengthRule(var maxLength: Int) : CapacityDraftRule {
    override fun check(item: CapacityDraft): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityDraftAmountRangeRule(var min: Double, var max: Double) : CapacityDraftRule {
    override fun check(item: CapacityDraft): MutableList<String> {
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

/** Runs every rule of a capacity draft. */
data class CapacityDraftValidator(var rules: MutableList<CapacityDraftRule>) {
    fun validate(item: CapacityDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacityDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityDraftValidator(): CapacityDraftValidator {
    return CapacityDraftValidator(
        mutableListOf<CapacityDraftRule>(
            CapacityDraftRequiredTitleRule("capacity draft needs an entry"),
            CapacityDraftRequiredUnitRule("capacity draft needs an entry"),
            CapacityDraftUnitLengthRule(16),
            CapacityDraftAmountRangeRule(6.0, 2558.8),
        ),
    )
}
