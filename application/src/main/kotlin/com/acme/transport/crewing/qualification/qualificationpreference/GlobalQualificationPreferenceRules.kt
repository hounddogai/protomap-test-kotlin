package com.acme.transport.crewing.qualification.qualificationpreference

/** A rule a global qualification preference must pass. Check returns its problems, or none. */
interface GlobalQualificationPreferenceRule {
    fun check(item: GlobalQualificationPreference): MutableList<String>
}

data class GlobalQualificationPreferenceCarrierLengthRule(
    var maxLength: Int,
) : GlobalQualificationPreferenceRule {
    override fun check(item: GlobalQualificationPreference): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalQualificationPreferenceArchivedGuardRule(var limit: Int) : GlobalQualificationPreferenceRule {
    override fun check(item: GlobalQualificationPreference): MutableList<String> {
        if (item.archived && item.createdAt > limit) {
            return mutableListOf<String>("created at ${item.createdAt} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class GlobalQualificationPreferenceRequiredCarrierRule(
    var message: String,
) : GlobalQualificationPreferenceRule {
    override fun check(item: GlobalQualificationPreference): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global qualification preference. */
data class GlobalQualificationPreferenceValidator(var rules: MutableList<GlobalQualificationPreferenceRule>) {
    fun validate(item: GlobalQualificationPreference): MutableList<String> {
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

    fun requireValid(item: GlobalQualificationPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalQualificationPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalQualificationPreferenceValidator(): GlobalQualificationPreferenceValidator {
    return GlobalQualificationPreferenceValidator(
        mutableListOf<GlobalQualificationPreferenceRule>(
            GlobalQualificationPreferenceCarrierLengthRule(16),
            GlobalQualificationPreferenceArchivedGuardRule(255),
            GlobalQualificationPreferenceRequiredCarrierRule("global qualification preference needs an entry"),
        ),
    )
}
