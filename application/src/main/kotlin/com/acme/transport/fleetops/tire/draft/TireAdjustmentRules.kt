package com.acme.transport.fleetops.tire.draft

/** A rule a tire adjustment must pass. Check returns its problems, or none. */
interface TireAdjustmentRule {
    fun check(item: TireAdjustment): MutableList<String>
}

data class TireAdjustmentRequiredNameRule(var message: String) : TireAdjustmentRule {
    override fun check(item: TireAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireAdjustmentRequiredCodeRule(var message: String) : TireAdjustmentRule {
    override fun check(item: TireAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireAdjustmentLoadKgRangeRule(var min: Double, var max: Double) : TireAdjustmentRule {
    override fun check(item: TireAdjustment): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire adjustment. */
data class TireAdjustmentValidator(var rules: MutableList<TireAdjustmentRule>) {
    fun validate(item: TireAdjustment): MutableList<String> {
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

    fun requireValid(item: TireAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultTireAdjustmentValidator(): TireAdjustmentValidator {
    return TireAdjustmentValidator(
        mutableListOf<TireAdjustmentRule>(
            TireAdjustmentRequiredNameRule("tire adjustment needs this field"),
            TireAdjustmentRequiredCodeRule("tire adjustment needs this field"),
            TireAdjustmentLoadKgRangeRule(0.5, 4806.2),
        ),
    )
}
