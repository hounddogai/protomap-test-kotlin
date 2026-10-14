package com.acme.transport.freightpay.fuelsurcharge.adjustment

/** A rule a fuel surcharge assignment must pass. Check returns its problems, or none. */
interface FuelSurchargeAssignmentRule {
    fun check(item: FuelSurchargeAssignment): MutableList<String>
}

data class FuelSurchargeAssignmentTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : FuelSurchargeAssignmentRule {
    override fun check(item: FuelSurchargeAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class FuelSurchargeAssignmentCodePrefixRule(var prefix: String) : FuelSurchargeAssignmentRule {
    override fun check(item: FuelSurchargeAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeAssignmentPalletsRangeRule(var min: Int, var max: Int) : FuelSurchargeAssignmentRule {
    override fun check(item: FuelSurchargeAssignment): MutableList<String> {
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

data class FuelSurchargeAssignmentVersionRangeRule(var min: Int, var max: Int) : FuelSurchargeAssignmentRule {
    override fun check(item: FuelSurchargeAssignment): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeAssignmentNameLengthRule(var maxLength: Int) : FuelSurchargeAssignmentRule {
    override fun check(item: FuelSurchargeAssignment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeAssignmentSequenceRangeRule(var min: Int, var max: Int) : FuelSurchargeAssignmentRule {
    override fun check(item: FuelSurchargeAssignment): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fuel surcharge assignment. */
data class FuelSurchargeAssignmentValidator(var rules: MutableList<FuelSurchargeAssignmentRule>) {
    fun validate(item: FuelSurchargeAssignment): MutableList<String> {
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

    fun requireValid(item: FuelSurchargeAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeAssignmentValidator(): FuelSurchargeAssignmentValidator {
    return FuelSurchargeAssignmentValidator(
        mutableListOf<FuelSurchargeAssignmentRule>(
            FuelSurchargeAssignmentTagsLimitRule(120, mutableListOf<String>("default", "regional", "scheduled")),
            FuelSurchargeAssignmentCodePrefixRule("ACM"),
            FuelSurchargeAssignmentPalletsRangeRule(32, 64),
            FuelSurchargeAssignmentVersionRangeRule(16, 86400),
            FuelSurchargeAssignmentNameLengthRule(120),
            FuelSurchargeAssignmentSequenceRangeRule(120, 86400),
        ),
    )
}
