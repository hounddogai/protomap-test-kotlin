package com.acme.transport.fleetops.defect.budget

/** A rule a defect baseline must pass. Check returns its problems, or none. */
interface DefectBaselineRule {
    fun check(item: DefectBaseline): MutableList<String>
}

data class DefectBaselineAmountRangeRule(var min: Double, var max: Double) : DefectBaselineRule {
    override fun check(item: DefectBaseline): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefectBaselineEtaMinutesRangeRule(var min: Int, var max: Int) : DefectBaselineRule {
    override fun check(item: DefectBaseline): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class DefectBaselineDurationMinutesRangeRule(var min: Int, var max: Int) : DefectBaselineRule {
    override fun check(item: DefectBaseline): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefectBaselineCodePrefixRule(var prefix: String) : DefectBaselineRule {
    override fun check(item: DefectBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect baseline. */
data class DefectBaselineValidator(var rules: MutableList<DefectBaselineRule>) {
    fun validate(item: DefectBaseline): MutableList<String> {
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

    fun requireValid(item: DefectBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectBaselineValidator(): DefectBaselineValidator {
    return DefectBaselineValidator(
        mutableListOf<DefectBaselineRule>(
            DefectBaselineAmountRangeRule(4.4, 4874.9),
            DefectBaselineEtaMinutesRangeRule(32, 1000),
            DefectBaselineDurationMinutesRangeRule(80, 1000),
            DefectBaselineCodePrefixRule("ACM"),
        ),
    )
}
