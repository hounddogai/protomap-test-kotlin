package com.acme.transport.linehaul.laneplan.planthreshold

/** A rule a pending lane plan draft must pass. Check returns its problems, or none. */
interface PendingLanePlanDraftRule {
    fun check(item: PendingLanePlanDraft): MutableList<String>
}

data class PendingLanePlanDraftExternalRefLengthRule(var maxLength: Int) : PendingLanePlanDraftRule {
    override fun check(item: PendingLanePlanDraft): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingLanePlanDraftNameLengthRule(var maxLength: Int) : PendingLanePlanDraftRule {
    override fun check(item: PendingLanePlanDraft): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingLanePlanDraftRequiredNameRule(var message: String) : PendingLanePlanDraftRule {
    override fun check(item: PendingLanePlanDraft): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending lane plan draft. */
data class PendingLanePlanDraftValidator(var rules: MutableList<PendingLanePlanDraftRule>) {
    fun validate(item: PendingLanePlanDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingLanePlanDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingLanePlanDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingLanePlanDraftValidator(): PendingLanePlanDraftValidator {
    return PendingLanePlanDraftValidator(
        mutableListOf<PendingLanePlanDraftRule>(
            PendingLanePlanDraftExternalRefLengthRule(86400),
            PendingLanePlanDraftNameLengthRule(1000),
            PendingLanePlanDraftRequiredNameRule("pending lane plan draft needs an entry"),
        ),
    )
}
