package com.acme.transport.terminal.dwell.mapping

/** A rule a dwell mapping must pass. Check returns its problems, or none. */
interface DwellMappingRule {
    fun check(item: DwellMapping): MutableList<String>
}

data class DwellMappingOdometerRangeRule(var min: Int, var max: Int) : DwellMappingRule {
    override fun check(item: DwellMapping): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class DwellMappingRequiredTitleRule(var message: String) : DwellMappingRule {
    override fun check(item: DwellMapping): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellMappingFuelLitersRangeRule(var min: Double, var max: Double) : DwellMappingRule {
    override fun check(item: DwellMapping): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DwellMappingNotesLengthRule(var maxLength: Int) : DwellMappingRule {
    override fun check(item: DwellMapping): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell mapping. */
data class DwellMappingValidator(var rules: MutableList<DwellMappingRule>) {
    fun validate(item: DwellMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DwellMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellMappingValidator(): DwellMappingValidator {
    return DwellMappingValidator(
        mutableListOf<DwellMappingRule>(
            DwellMappingOdometerRangeRule(120, 3600),
            DwellMappingRequiredTitleRule("dwell mapping needs an entry"),
            DwellMappingFuelLitersRangeRule(3.5, 4525.9),
            DwellMappingNotesLengthRule(64),
        ),
    )
}
