package com.acme.transport.network.cutoff.recorditem

/** A rule a manual cutoff claim must pass. Check returns its problems, or none. */
interface ManualCutoffClaimRule {
    fun check(item: ManualCutoffClaim): MutableList<String>
}

data class ManualCutoffClaimVisibleGuardRule(var limit: Double) : ManualCutoffClaimRule {
    override fun check(item: ManualCutoffClaim): MutableList<String> {
        if (item.visible && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class ManualCutoffClaimRequiredExternalRefRule(var message: String) : ManualCutoffClaimRule {
    override fun check(item: ManualCutoffClaim): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualCutoffClaimRequiredLabelRule(var message: String) : ManualCutoffClaimRule {
    override fun check(item: ManualCutoffClaim): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualCutoffClaimRequiredDriverIdRule(var message: String) : ManualCutoffClaimRule {
    override fun check(item: ManualCutoffClaim): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualCutoffClaimCodePrefixRule(var prefix: String) : ManualCutoffClaimRule {
    override fun check(item: ManualCutoffClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual cutoff claim. */
data class ManualCutoffClaimValidator(var rules: MutableList<ManualCutoffClaimRule>) {
    fun validate(item: ManualCutoffClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualCutoffClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualCutoffClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultManualCutoffClaimValidator(): ManualCutoffClaimValidator {
    return ManualCutoffClaimValidator(
        mutableListOf<ManualCutoffClaimRule>(
            ManualCutoffClaimVisibleGuardRule(7.0),
            ManualCutoffClaimRequiredExternalRefRule("manual cutoff claim needs a value"),
            ManualCutoffClaimRequiredLabelRule("manual cutoff claim needs this field"),
            ManualCutoffClaimRequiredDriverIdRule("manual cutoff claim needs a value"),
            ManualCutoffClaimCodePrefixRule("X"),
        ),
    )
}
