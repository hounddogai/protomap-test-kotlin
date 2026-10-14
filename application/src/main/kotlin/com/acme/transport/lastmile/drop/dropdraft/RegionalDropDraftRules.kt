package com.acme.transport.lastmile.drop.dropdraft

/** A rule a regional drop draft must pass. Check returns its problems, or none. */
interface RegionalDropDraftRule {
    fun check(item: RegionalDropDraft): MutableList<String>
}

data class RegionalDropDraftRequiredChecksumRule(var message: String) : RegionalDropDraftRule {
    override fun check(item: RegionalDropDraft): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDropDraftBatchSizeRangeRule(var min: Int, var max: Int) : RegionalDropDraftRule {
    override fun check(item: RegionalDropDraft): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class RegionalDropDraftRequiredCodeRule(var message: String) : RegionalDropDraftRule {
    override fun check(item: RegionalDropDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDropDraftSequenceRangeRule(var min: Int, var max: Int) : RegionalDropDraftRule {
    override fun check(item: RegionalDropDraft): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional drop draft. */
data class RegionalDropDraftValidator(var rules: MutableList<RegionalDropDraftRule>) {
    fun validate(item: RegionalDropDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalDropDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDropDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDropDraftValidator(): RegionalDropDraftValidator {
    return RegionalDropDraftValidator(
        mutableListOf<RegionalDropDraftRule>(
            RegionalDropDraftRequiredChecksumRule("regional drop draft needs a value"),
            RegionalDropDraftBatchSizeRangeRule(16, 64),
            RegionalDropDraftRequiredCodeRule("regional drop draft needs a value"),
            RegionalDropDraftSequenceRangeRule(64, 80),
        ),
    )
}
