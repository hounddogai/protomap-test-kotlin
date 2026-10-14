package com.acme.transport.customs.declaration.threshold

/** A rule a declaration target must pass. Check returns its problems, or none. */
interface DeclarationTargetRule {
    fun check(item: DeclarationTarget): MutableList<String>
}

data class DeclarationTargetRequiredVehicleIdRule(var message: String) : DeclarationTargetRule {
    override fun check(item: DeclarationTarget): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationTargetCostRangeRule(var min: Double, var max: Double) : DeclarationTargetRule {
    override fun check(item: DeclarationTarget): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationTargetPinnedGuardRule(var limit: Double) : DeclarationTargetRule {
    override fun check(item: DeclarationTarget): MutableList<String> {
        if (item.pinned && item.budget > limit) {
            return mutableListOf<String>("budget ${item.budget} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationTargetRequiredNameRule(var message: String) : DeclarationTargetRule {
    override fun check(item: DeclarationTarget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration target. */
data class DeclarationTargetValidator(var rules: MutableList<DeclarationTargetRule>) {
    fun validate(item: DeclarationTarget): MutableList<String> {
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

    fun requireValid(item: DeclarationTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationTargetValidator(): DeclarationTargetValidator {
    return DeclarationTargetValidator(
        mutableListOf<DeclarationTargetRule>(
            DeclarationTargetRequiredVehicleIdRule("declaration target needs a value"),
            DeclarationTargetCostRangeRule(3.8, 2963.9),
            DeclarationTargetPinnedGuardRule(4.5),
            DeclarationTargetRequiredNameRule("declaration target needs this field"),
        ),
    )
}
