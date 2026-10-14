package com.acme.transport.network.cutoff.cutofftarget

/** A rule a manual cutoff draft must pass. Check returns its problems, or none. */
interface ManualCutoffDraftRule {
    fun check(item: ManualCutoffDraft): MutableList<String>
}

data class ManualCutoffDraftCodeLengthRule(var maxLength: Int) : ManualCutoffDraftRule {
    override fun check(item: ManualCutoffDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualCutoffDraftRequiredDepotRule(var message: String) : ManualCutoffDraftRule {
    override fun check(item: ManualCutoffDraft): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualCutoffDraftDepotLengthRule(var maxLength: Int) : ManualCutoffDraftRule {
    override fun check(item: ManualCutoffDraft): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualCutoffDraftRateRangeRule(var min: Double, var max: Double) : ManualCutoffDraftRule {
    override fun check(item: ManualCutoffDraft): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual cutoff draft. */
data class ManualCutoffDraftValidator(var rules: MutableList<ManualCutoffDraftRule>) {
    fun validate(item: ManualCutoffDraft): MutableList<String> {
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

    fun requireValid(item: ManualCutoffDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualCutoffDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultManualCutoffDraftValidator(): ManualCutoffDraftValidator {
    return ManualCutoffDraftValidator(
        mutableListOf<ManualCutoffDraftRule>(
            ManualCutoffDraftCodeLengthRule(16),
            ManualCutoffDraftRequiredDepotRule("manual cutoff draft needs a value"),
            ManualCutoffDraftDepotLengthRule(86400),
            ManualCutoffDraftRateRangeRule(0.5, 3730.7),
        ),
    )
}
