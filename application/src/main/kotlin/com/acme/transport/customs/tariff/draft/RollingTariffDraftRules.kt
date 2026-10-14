package com.acme.transport.customs.tariff.draft

/** A rule a rolling tariff draft must pass. Check returns its problems, or none. */
interface RollingTariffDraftRule {
    fun check(item: RollingTariffDraft): MutableList<String>
}

data class RollingTariffDraftLabelLengthRule(var maxLength: Int) : RollingTariffDraftRule {
    override fun check(item: RollingTariffDraft): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingTariffDraftEtaMinutesRangeRule(var min: Int, var max: Int) : RollingTariffDraftRule {
    override fun check(item: RollingTariffDraft): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingTariffDraftFloorValueRangeRule(var min: Double, var max: Double) : RollingTariffDraftRule {
    override fun check(item: RollingTariffDraft): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rolling tariff draft. */
data class RollingTariffDraftValidator(var rules: MutableList<RollingTariffDraftRule>) {
    fun validate(item: RollingTariffDraft): MutableList<String> {
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

    fun requireValid(item: RollingTariffDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingTariffDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingTariffDraftValidator(): RollingTariffDraftValidator {
    return RollingTariffDraftValidator(
        mutableListOf<RollingTariffDraftRule>(
            RollingTariffDraftLabelLengthRule(80),
            RollingTariffDraftEtaMinutesRangeRule(80, 255),
            RollingTariffDraftFloorValueRangeRule(1.8, 4161.8),
        ),
    )
}
