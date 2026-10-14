package com.acme.transport.linehaul.backhaul.rule

/** A rule a manual backhaul claim must pass. Check returns its problems, or none. */
interface ManualBackhaulClaimRule {
    fun check(item: ManualBackhaulClaim): MutableList<String>
}

data class ManualBackhaulClaimSequenceRangeRule(var min: Int, var max: Int) : ManualBackhaulClaimRule {
    override fun check(item: ManualBackhaulClaim): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualBackhaulClaimVisibleGuardRule(var limit: Double) : ManualBackhaulClaimRule {
    override fun check(item: ManualBackhaulClaim): MutableList<String> {
        if (item.visible && item.maximum > limit) {
            return mutableListOf<String>("maximum ${item.maximum} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class ManualBackhaulClaimLabelLengthRule(var maxLength: Int) : ManualBackhaulClaimRule {
    override fun check(item: ManualBackhaulClaim): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualBackhaulClaimRequiredDockRule(var message: String) : ManualBackhaulClaimRule {
    override fun check(item: ManualBackhaulClaim): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualBackhaulClaimCeilingRangeRule(var min: Double, var max: Double) : ManualBackhaulClaimRule {
    override fun check(item: ManualBackhaulClaim): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a manual backhaul claim. */
data class ManualBackhaulClaimValidator(var rules: MutableList<ManualBackhaulClaimRule>) {
    fun validate(item: ManualBackhaulClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualBackhaulClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualBackhaulClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultManualBackhaulClaimValidator(): ManualBackhaulClaimValidator {
    return ManualBackhaulClaimValidator(
        mutableListOf<ManualBackhaulClaimRule>(
            ManualBackhaulClaimSequenceRangeRule(255, 86400),
            ManualBackhaulClaimVisibleGuardRule(7.7),
            ManualBackhaulClaimLabelLengthRule(80),
            ManualBackhaulClaimRequiredDockRule("manual backhaul claim needs a value"),
            ManualBackhaulClaimCeilingRangeRule(8.1, 1220.8),
        ),
    )
}
