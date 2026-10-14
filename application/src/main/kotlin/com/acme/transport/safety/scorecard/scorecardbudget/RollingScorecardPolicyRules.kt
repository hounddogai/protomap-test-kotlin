package com.acme.transport.safety.scorecard.scorecardbudget

/** A rule a rolling scorecard policy must pass. Check returns its problems, or none. */
interface RollingScorecardPolicyRule {
    fun check(item: RollingScorecardPolicy): MutableList<String>
}

data class RollingScorecardPolicyNotesLengthRule(var maxLength: Int) : RollingScorecardPolicyRule {
    override fun check(item: RollingScorecardPolicy): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingScorecardPolicyCodePrefixRule(var prefix: String) : RollingScorecardPolicyRule {
    override fun check(item: RollingScorecardPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingScorecardPolicyRequiredLabelRule(var message: String) : RollingScorecardPolicyRule {
    override fun check(item: RollingScorecardPolicy): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingScorecardPolicyCodeLengthRule(var maxLength: Int) : RollingScorecardPolicyRule {
    override fun check(item: RollingScorecardPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingScorecardPolicyRequiredNotesRule(var message: String) : RollingScorecardPolicyRule {
    override fun check(item: RollingScorecardPolicy): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling scorecard policy. */
data class RollingScorecardPolicyValidator(var rules: MutableList<RollingScorecardPolicyRule>) {
    fun validate(item: RollingScorecardPolicy): MutableList<String> {
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

    fun requireValid(item: RollingScorecardPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingScorecardPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingScorecardPolicyValidator(): RollingScorecardPolicyValidator {
    return RollingScorecardPolicyValidator(
        mutableListOf<RollingScorecardPolicyRule>(
            RollingScorecardPolicyNotesLengthRule(86400),
            RollingScorecardPolicyCodePrefixRule("OPS"),
            RollingScorecardPolicyRequiredLabelRule("rolling scorecard policy needs an entry"),
            RollingScorecardPolicyCodeLengthRule(32),
            RollingScorecardPolicyRequiredNotesRule("rolling scorecard policy needs this field"),
        ),
    )
}
