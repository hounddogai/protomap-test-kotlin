package com.acme.transport.safety.incident.proposal

/** A rule a incident target must pass. Check returns its problems, or none. */
interface IncidentTargetRule {
    fun check(item: IncidentTarget): MutableList<String>
}

data class IncidentTargetEtaMinutesRangeRule(var min: Int, var max: Int) : IncidentTargetRule {
    override fun check(item: IncidentTarget): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IncidentTargetCodeLengthRule(var maxLength: Int) : IncidentTargetRule {
    override fun check(item: IncidentTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentTargetNameLengthRule(var maxLength: Int) : IncidentTargetRule {
    override fun check(item: IncidentTarget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentTargetCostRangeRule(var min: Double, var max: Double) : IncidentTargetRule {
    override fun check(item: IncidentTarget): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IncidentTargetBudgetRangeRule(var min: Double, var max: Double) : IncidentTargetRule {
    override fun check(item: IncidentTarget): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IncidentTargetCodePrefixRule(var prefix: String) : IncidentTargetRule {
    override fun check(item: IncidentTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident target. */
data class IncidentTargetValidator(var rules: MutableList<IncidentTargetRule>) {
    fun validate(item: IncidentTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IncidentTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentTargetValidator(): IncidentTargetValidator {
    return IncidentTargetValidator(
        mutableListOf<IncidentTargetRule>(
            IncidentTargetEtaMinutesRangeRule(16, 64),
            IncidentTargetCodeLengthRule(255),
            IncidentTargetNameLengthRule(120),
            IncidentTargetCostRangeRule(5.5, 2191.2),
            IncidentTargetBudgetRangeRule(1.8, 1742.0),
            IncidentTargetCodePrefixRule("X"),
        ),
    )
}
