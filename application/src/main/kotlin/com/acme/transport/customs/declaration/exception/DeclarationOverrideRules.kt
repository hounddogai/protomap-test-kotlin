package com.acme.transport.customs.declaration.exception

/** A rule a declaration override must pass. Check returns its problems, or none. */
interface DeclarationOverrideRule {
    fun check(item: DeclarationOverride): MutableList<String>
}

data class DeclarationOverrideNameLengthRule(var maxLength: Int) : DeclarationOverrideRule {
    override fun check(item: DeclarationOverride): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationOverrideRequiredCodeRule(var message: String) : DeclarationOverrideRule {
    override fun check(item: DeclarationOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationOverrideRequiredVehicleIdRule(var message: String) : DeclarationOverrideRule {
    override fun check(item: DeclarationOverride): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationOverrideAmountRangeRule(var min: Double, var max: Double) : DeclarationOverrideRule {
    override fun check(item: DeclarationOverride): MutableList<String> {
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

data class DeclarationOverrideVehicleIdLengthRule(var maxLength: Int) : DeclarationOverrideRule {
    override fun check(item: DeclarationOverride): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration override. */
data class DeclarationOverrideValidator(var rules: MutableList<DeclarationOverrideRule>) {
    fun validate(item: DeclarationOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DeclarationOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationOverrideValidator(): DeclarationOverrideValidator {
    return DeclarationOverrideValidator(
        mutableListOf<DeclarationOverrideRule>(
            DeclarationOverrideNameLengthRule(80),
            DeclarationOverrideRequiredCodeRule("declaration override needs an entry"),
            DeclarationOverrideRequiredVehicleIdRule("declaration override needs a value"),
            DeclarationOverrideAmountRangeRule(3.1, 384.6),
            DeclarationOverrideVehicleIdLengthRule(32),
        ),
    )
}
