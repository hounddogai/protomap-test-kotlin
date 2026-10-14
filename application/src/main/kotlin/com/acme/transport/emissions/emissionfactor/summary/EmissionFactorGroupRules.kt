package com.acme.transport.emissions.emissionfactor.summary

/** A rule a emission factor group must pass. Check returns its problems, or none. */
interface EmissionFactorGroupRule {
    fun check(item: EmissionFactorGroup): MutableList<String>
}

data class EmissionFactorGroupRequiredLabelRule(var message: String) : EmissionFactorGroupRule {
    override fun check(item: EmissionFactorGroup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorGroupApprovedGuardRule(var limit: Int) : EmissionFactorGroupRule {
    override fun check(item: EmissionFactorGroup): MutableList<String> {
        if (item.approved && item.revision > limit) {
            return mutableListOf<String>("revision ${item.revision} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorGroupNotesLengthRule(var maxLength: Int) : EmissionFactorGroupRule {
    override fun check(item: EmissionFactorGroup): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorGroupRevisionRangeRule(var min: Int, var max: Int) : EmissionFactorGroupRule {
    override fun check(item: EmissionFactorGroup): MutableList<String> {
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

data class EmissionFactorGroupCodeLengthRule(var maxLength: Int) : EmissionFactorGroupRule {
    override fun check(item: EmissionFactorGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor group. */
data class EmissionFactorGroupValidator(var rules: MutableList<EmissionFactorGroupRule>) {
    fun validate(item: EmissionFactorGroup): MutableList<String> {
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

    fun requireValid(item: EmissionFactorGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorGroupValidator(): EmissionFactorGroupValidator {
    return EmissionFactorGroupValidator(
        mutableListOf<EmissionFactorGroupRule>(
            EmissionFactorGroupRequiredLabelRule("emission factor group needs an entry"),
            EmissionFactorGroupApprovedGuardRule(255),
            EmissionFactorGroupNotesLengthRule(120),
            EmissionFactorGroupRevisionRangeRule(255, 255),
            EmissionFactorGroupCodeLengthRule(80),
        ),
    )
}
