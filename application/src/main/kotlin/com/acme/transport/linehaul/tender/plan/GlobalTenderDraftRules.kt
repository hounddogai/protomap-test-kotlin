package com.acme.transport.linehaul.tender.plan

/** A rule a global tender draft must pass. Check returns its problems, or none. */
interface GlobalTenderDraftRule {
    fun check(item: GlobalTenderDraft): MutableList<String>
}

data class GlobalTenderDraftRequiredCodeRule(var message: String) : GlobalTenderDraftRule {
    override fun check(item: GlobalTenderDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderDraftRequiredNameRule(var message: String) : GlobalTenderDraftRule {
    override fun check(item: GlobalTenderDraft): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderDraftRequiredRegionRule(var message: String) : GlobalTenderDraftRule {
    override fun check(item: GlobalTenderDraft): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderDraftNameLengthRule(var maxLength: Int) : GlobalTenderDraftRule {
    override fun check(item: GlobalTenderDraft): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global tender draft. */
data class GlobalTenderDraftValidator(var rules: MutableList<GlobalTenderDraftRule>) {
    fun validate(item: GlobalTenderDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalTenderDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTenderDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTenderDraftValidator(): GlobalTenderDraftValidator {
    return GlobalTenderDraftValidator(
        mutableListOf<GlobalTenderDraftRule>(
            GlobalTenderDraftRequiredCodeRule("global tender draft needs this field"),
            GlobalTenderDraftRequiredNameRule("global tender draft needs a value"),
            GlobalTenderDraftRequiredRegionRule("global tender draft needs a value"),
            GlobalTenderDraftNameLengthRule(120),
        ),
    )
}
