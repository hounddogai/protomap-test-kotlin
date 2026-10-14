package com.acme.transport.fleetops.inspection.group

/** A rule a inspection window must pass. Check returns its problems, or none. */
interface InspectionWindowRule {
    fun check(item: InspectionWindow): MutableList<String>
}

data class InspectionWindowRequiredNameRule(var message: String) : InspectionWindowRule {
    override fun check(item: InspectionWindow): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionWindowAmountRangeRule(var min: Double, var max: Double) : InspectionWindowRule {
    override fun check(item: InspectionWindow): MutableList<String> {
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

data class InspectionWindowCodePrefixRule(var prefix: String) : InspectionWindowRule {
    override fun check(item: InspectionWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionWindowQuantityRangeRule(var min: Int, var max: Int) : InspectionWindowRule {
    override fun check(item: InspectionWindow): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection window. */
data class InspectionWindowValidator(var rules: MutableList<InspectionWindowRule>) {
    fun validate(item: InspectionWindow): MutableList<String> {
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

    fun requireValid(item: InspectionWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionWindowValidator(): InspectionWindowValidator {
    return InspectionWindowValidator(
        mutableListOf<InspectionWindowRule>(
            InspectionWindowRequiredNameRule("inspection window needs this field"),
            InspectionWindowAmountRangeRule(4.3, 2058.5),
            InspectionWindowCodePrefixRule("ACM"),
            InspectionWindowQuantityRangeRule(32, 120),
        ),
    )
}
