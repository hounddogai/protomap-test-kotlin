package com.acme.transport.emissions.fuelburn.review

/** A rule a fuel burn rule must pass. Check returns its problems, or none. */
interface FuelBurnRuleRule {
    fun check(item: FuelBurnRule): MutableList<String>
}

data class FuelBurnRuleRevisionRangeRule(var min: Int, var max: Int) : FuelBurnRuleRule {
    override fun check(item: FuelBurnRule): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class FuelBurnRuleRequiredCodeRule(var message: String) : FuelBurnRuleRule {
    override fun check(item: FuelBurnRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnRuleRequiredPhoneRule(var message: String) : FuelBurnRuleRule {
    override fun check(item: FuelBurnRule): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnRuleNotesLengthRule(var maxLength: Int) : FuelBurnRuleRule {
    override fun check(item: FuelBurnRule): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnRuleRequiredNameRule(var message: String) : FuelBurnRuleRule {
    override fun check(item: FuelBurnRule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn rule. */
data class FuelBurnRuleValidator(var rules: MutableList<FuelBurnRuleRule>) {
    fun validate(item: FuelBurnRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelBurnRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnRule>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnRuleValidator(): FuelBurnRuleValidator {
    return FuelBurnRuleValidator(
        mutableListOf<FuelBurnRuleRule>(
            FuelBurnRuleRevisionRangeRule(64, 80),
            FuelBurnRuleRequiredCodeRule("fuel burn rule needs a value"),
            FuelBurnRuleRequiredPhoneRule("fuel burn rule needs a value"),
            FuelBurnRuleNotesLengthRule(1000),
            FuelBurnRuleRequiredNameRule("fuel burn rule needs a value"),
        ),
    )
}
