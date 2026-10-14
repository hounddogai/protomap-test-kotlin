package com.acme.transport.linehaul.laneplan.target

/** A rule a manual lane plan target must pass. Check returns its problems, or none. */
interface ManualLanePlanTargetRule {
    fun check(item: ManualLanePlanTarget): MutableList<String>
}

data class ManualLanePlanTargetRequiredContactEmailRule(var message: String) : ManualLanePlanTargetRule {
    override fun check(item: ManualLanePlanTarget): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualLanePlanTargetLabelLengthRule(var maxLength: Int) : ManualLanePlanTargetRule {
    override fun check(item: ManualLanePlanTarget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualLanePlanTargetContactEmailLengthRule(var maxLength: Int) : ManualLanePlanTargetRule {
    override fun check(item: ManualLanePlanTarget): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualLanePlanTargetApprovedGuardRule(var limit: Int) : ManualLanePlanTargetRule {
    override fun check(item: ManualLanePlanTarget): MutableList<String> {
        if (item.approved && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual lane plan target. */
data class ManualLanePlanTargetValidator(var rules: MutableList<ManualLanePlanTargetRule>) {
    fun validate(item: ManualLanePlanTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualLanePlanTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualLanePlanTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultManualLanePlanTargetValidator(): ManualLanePlanTargetValidator {
    return ManualLanePlanTargetValidator(
        mutableListOf<ManualLanePlanTargetRule>(
            ManualLanePlanTargetRequiredContactEmailRule("manual lane plan target needs an entry"),
            ManualLanePlanTargetLabelLengthRule(80),
            ManualLanePlanTargetContactEmailLengthRule(86400),
            ManualLanePlanTargetApprovedGuardRule(80),
        ),
    )
}
