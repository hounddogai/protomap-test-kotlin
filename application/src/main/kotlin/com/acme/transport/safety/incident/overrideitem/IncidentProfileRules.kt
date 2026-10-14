package com.acme.transport.safety.incident.overrideitem

/** A rule a incident profile must pass. Check returns its problems, or none. */
interface IncidentProfileRule {
    fun check(item: IncidentProfile): MutableList<String>
}

data class IncidentProfileAmountRangeRule(var min: Double, var max: Double) : IncidentProfileRule {
    override fun check(item: IncidentProfile): MutableList<String> {
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

data class IncidentProfileCodeLengthRule(var maxLength: Int) : IncidentProfileRule {
    override fun check(item: IncidentProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentProfileLaneLengthRule(var maxLength: Int) : IncidentProfileRule {
    override fun check(item: IncidentProfile): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentProfileUnitLengthRule(var maxLength: Int) : IncidentProfileRule {
    override fun check(item: IncidentProfile): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentProfileRequiredLabelRule(var message: String) : IncidentProfileRule {
    override fun check(item: IncidentProfile): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident profile. */
data class IncidentProfileValidator(var rules: MutableList<IncidentProfileRule>) {
    fun validate(item: IncidentProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IncidentProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentProfileValidator(): IncidentProfileValidator {
    return IncidentProfileValidator(
        mutableListOf<IncidentProfileRule>(
            IncidentProfileAmountRangeRule(5.6, 2448.0),
            IncidentProfileCodeLengthRule(64),
            IncidentProfileLaneLengthRule(1000),
            IncidentProfileUnitLengthRule(255),
            IncidentProfileRequiredLabelRule("incident profile needs this field"),
        ),
    )
}
