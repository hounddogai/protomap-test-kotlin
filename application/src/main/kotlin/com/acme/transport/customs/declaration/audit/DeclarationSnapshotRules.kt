package com.acme.transport.customs.declaration.audit

/** A rule a declaration snapshot must pass. Check returns its problems, or none. */
interface DeclarationSnapshotRule {
    fun check(item: DeclarationSnapshot): MutableList<String>
}

data class DeclarationSnapshotRequiredRouteCodeRule(var message: String) : DeclarationSnapshotRule {
    override fun check(item: DeclarationSnapshot): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationSnapshotCapacityRangeRule(var min: Int, var max: Int) : DeclarationSnapshotRule {
    override fun check(item: DeclarationSnapshot): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class DeclarationSnapshotDiscountRangeRule(var min: Double, var max: Double) : DeclarationSnapshotRule {
    override fun check(item: DeclarationSnapshot): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationSnapshotGraceDaysRangeRule(var min: Int, var max: Int) : DeclarationSnapshotRule {
    override fun check(item: DeclarationSnapshot): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class DeclarationSnapshotRouteCodeLengthRule(var maxLength: Int) : DeclarationSnapshotRule {
    override fun check(item: DeclarationSnapshot): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration snapshot. */
data class DeclarationSnapshotValidator(var rules: MutableList<DeclarationSnapshotRule>) {
    fun validate(item: DeclarationSnapshot): MutableList<String> {
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

    fun requireValid(item: DeclarationSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationSnapshotValidator(): DeclarationSnapshotValidator {
    return DeclarationSnapshotValidator(
        mutableListOf<DeclarationSnapshotRule>(
            DeclarationSnapshotRequiredRouteCodeRule("declaration snapshot needs this field"),
            DeclarationSnapshotCapacityRangeRule(80, 120),
            DeclarationSnapshotDiscountRangeRule(8.8, 4462.0),
            DeclarationSnapshotGraceDaysRangeRule(64, 1000),
            DeclarationSnapshotRouteCodeLengthRule(16),
        ),
    )
}
