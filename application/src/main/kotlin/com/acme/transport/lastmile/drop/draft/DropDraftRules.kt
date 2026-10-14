package com.acme.transport.lastmile.drop.draft

/** A rule a drop draft must pass. Check returns its problems, or none. */
interface DropDraftRule {
    fun check(item: DropDraft): MutableList<String>
}

data class DropDraftFuelLitersRangeRule(var min: Double, var max: Double) : DropDraftRule {
    override fun check(item: DropDraft): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DropDraftPalletsRangeRule(var min: Int, var max: Int) : DropDraftRule {
    override fun check(item: DropDraft): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class DropDraftLabelLengthRule(var maxLength: Int) : DropDraftRule {
    override fun check(item: DropDraft): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropDraftCodePrefixRule(var prefix: String) : DropDraftRule {
    override fun check(item: DropDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DropDraftRequiredCodeRule(var message: String) : DropDraftRule {
    override fun check(item: DropDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop draft. */
data class DropDraftValidator(var rules: MutableList<DropDraftRule>) {
    fun validate(item: DropDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DropDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultDropDraftValidator(): DropDraftValidator {
    return DropDraftValidator(
        mutableListOf<DropDraftRule>(
            DropDraftFuelLitersRangeRule(1.7, 1408.8),
            DropDraftPalletsRangeRule(80, 1000),
            DropDraftLabelLengthRule(80),
            DropDraftCodePrefixRule("ACM"),
            DropDraftRequiredCodeRule("drop draft needs this field"),
        ),
    )
}
