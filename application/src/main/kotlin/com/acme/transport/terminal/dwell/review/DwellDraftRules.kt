package com.acme.transport.terminal.dwell.review

/** A rule a dwell draft must pass. Check returns its problems, or none. */
interface DwellDraftRule {
    fun check(item: DwellDraft): MutableList<String>
}

data class DwellDraftRequiredUnitRule(var message: String) : DwellDraftRule {
    override fun check(item: DwellDraft): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellDraftUnitLengthRule(var maxLength: Int) : DwellDraftRule {
    override fun check(item: DwellDraft): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellDraftCostRangeRule(var min: Double, var max: Double) : DwellDraftRule {
    override fun check(item: DwellDraft): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class DwellDraftRequiredSegmentRule(var message: String) : DwellDraftRule {
    override fun check(item: DwellDraft): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellDraftRequiredCodeRule(var message: String) : DwellDraftRule {
    override fun check(item: DwellDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell draft. */
data class DwellDraftValidator(var rules: MutableList<DwellDraftRule>) {
    fun validate(item: DwellDraft): MutableList<String> {
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

    fun requireValid(item: DwellDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellDraftValidator(): DwellDraftValidator {
    return DwellDraftValidator(
        mutableListOf<DwellDraftRule>(
            DwellDraftRequiredUnitRule("dwell draft needs a value"),
            DwellDraftUnitLengthRule(86400),
            DwellDraftCostRangeRule(6.5, 4635.2),
            DwellDraftRequiredSegmentRule("dwell draft needs a value"),
            DwellDraftRequiredCodeRule("dwell draft needs an entry"),
        ),
    )
}
