package com.acme.transport.customs.clearance.clearanceestimate

/** A rule a legacy clearance target must pass. Check returns its problems, or none. */
interface LegacyClearanceTargetRule {
    fun check(item: LegacyClearanceTarget): MutableList<String>
}

data class LegacyClearanceTargetLocaleLengthRule(var maxLength: Int) : LegacyClearanceTargetRule {
    override fun check(item: LegacyClearanceTarget): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyClearanceTargetPalletsRangeRule(var min: Int, var max: Int) : LegacyClearanceTargetRule {
    override fun check(item: LegacyClearanceTarget): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyClearanceTargetRequiredNameRule(var message: String) : LegacyClearanceTargetRule {
    override fun check(item: LegacyClearanceTarget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyClearanceTargetPriorityRangeRule(var min: Int, var max: Int) : LegacyClearanceTargetRule {
    override fun check(item: LegacyClearanceTarget): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class LegacyClearanceTargetNameLengthRule(var maxLength: Int) : LegacyClearanceTargetRule {
    override fun check(item: LegacyClearanceTarget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyClearanceTargetRequiredCodeRule(var message: String) : LegacyClearanceTargetRule {
    override fun check(item: LegacyClearanceTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy clearance target. */
data class LegacyClearanceTargetValidator(var rules: MutableList<LegacyClearanceTargetRule>) {
    fun validate(item: LegacyClearanceTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyClearanceTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyClearanceTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyClearanceTargetValidator(): LegacyClearanceTargetValidator {
    return LegacyClearanceTargetValidator(
        mutableListOf<LegacyClearanceTargetRule>(
            LegacyClearanceTargetLocaleLengthRule(64),
            LegacyClearanceTargetPalletsRangeRule(16, 32),
            LegacyClearanceTargetRequiredNameRule("legacy clearance target needs a value"),
            LegacyClearanceTargetPriorityRangeRule(255, 3600),
            LegacyClearanceTargetNameLengthRule(255),
            LegacyClearanceTargetRequiredCodeRule("legacy clearance target needs an entry"),
        ),
    )
}
