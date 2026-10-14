package com.acme.transport.customs.hold.adaptiveholdmapping

/** A rule a primary hold mapping must pass. Check returns its problems, or none. */
interface PrimaryHoldMappingRule {
    fun check(item: PrimaryHoldMapping): MutableList<String>
}

data class PrimaryHoldMappingRequiredOwnerIdRule(var message: String) : PrimaryHoldMappingRule {
    override fun check(item: PrimaryHoldMapping): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryHoldMappingRequiredCodeRule(var message: String) : PrimaryHoldMappingRule {
    override fun check(item: PrimaryHoldMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryHoldMappingDiscountRangeRule(var min: Double, var max: Double) : PrimaryHoldMappingRule {
    override fun check(item: PrimaryHoldMapping): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary hold mapping. */
data class PrimaryHoldMappingValidator(var rules: MutableList<PrimaryHoldMappingRule>) {
    fun validate(item: PrimaryHoldMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryHoldMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryHoldMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryHoldMappingValidator(): PrimaryHoldMappingValidator {
    return PrimaryHoldMappingValidator(
        mutableListOf<PrimaryHoldMappingRule>(
            PrimaryHoldMappingRequiredOwnerIdRule("primary hold mapping needs a value"),
            PrimaryHoldMappingRequiredCodeRule("primary hold mapping needs a value"),
            PrimaryHoldMappingDiscountRangeRule(9.2, 1278.3),
        ),
    )
}
