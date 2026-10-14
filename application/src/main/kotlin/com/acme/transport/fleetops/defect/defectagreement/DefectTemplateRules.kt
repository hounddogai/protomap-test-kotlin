package com.acme.transport.fleetops.defect.defectagreement

/** A rule a defect template must pass. Check returns its problems, or none. */
interface DefectTemplateRule {
    fun check(item: DefectTemplate): MutableList<String>
}

data class DefectTemplateRequiredDockRule(var message: String) : DefectTemplateRule {
    override fun check(item: DefectTemplate): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectTemplateCapacityRangeRule(var min: Int, var max: Int) : DefectTemplateRule {
    override fun check(item: DefectTemplate): MutableList<String> {
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

data class DefectTemplateAttemptsRangeRule(var min: Int, var max: Int) : DefectTemplateRule {
    override fun check(item: DefectTemplate): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a defect template. */
data class DefectTemplateValidator(var rules: MutableList<DefectTemplateRule>) {
    fun validate(item: DefectTemplate): MutableList<String> {
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

    fun requireValid(item: DefectTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectTemplateValidator(): DefectTemplateValidator {
    return DefectTemplateValidator(
        mutableListOf<DefectTemplateRule>(
            DefectTemplateRequiredDockRule("defect template needs this field"),
            DefectTemplateCapacityRangeRule(32, 86400),
            DefectTemplateAttemptsRangeRule(1000, 86400),
        ),
    )
}
