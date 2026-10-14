package com.acme.transport.lastmile.locker.lockerclaim

/** A rule a fallback locker draft must pass. Check returns its problems, or none. */
interface FallbackLockerDraftRule {
    fun check(item: FallbackLockerDraft): MutableList<String>
}

data class FallbackLockerDraftRequiredRegionRule(var message: String) : FallbackLockerDraftRule {
    override fun check(item: FallbackLockerDraft): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackLockerDraftCodeLengthRule(var maxLength: Int) : FallbackLockerDraftRule {
    override fun check(item: FallbackLockerDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLockerDraftActiveGuardRule(var limit: Int) : FallbackLockerDraftRule {
    override fun check(item: FallbackLockerDraft): MutableList<String> {
        if (item.active && item.attempts > limit) {
            return mutableListOf<String>("attempts ${item.attempts} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLockerDraftRegionLengthRule(var maxLength: Int) : FallbackLockerDraftRule {
    override fun check(item: FallbackLockerDraft): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLockerDraftNotesLengthRule(var maxLength: Int) : FallbackLockerDraftRule {
    override fun check(item: FallbackLockerDraft): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback locker draft. */
data class FallbackLockerDraftValidator(var rules: MutableList<FallbackLockerDraftRule>) {
    fun validate(item: FallbackLockerDraft): MutableList<String> {
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

    fun requireValid(item: FallbackLockerDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackLockerDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackLockerDraftValidator(): FallbackLockerDraftValidator {
    return FallbackLockerDraftValidator(
        mutableListOf<FallbackLockerDraftRule>(
            FallbackLockerDraftRequiredRegionRule("fallback locker draft needs this field"),
            FallbackLockerDraftCodeLengthRule(64),
            FallbackLockerDraftActiveGuardRule(16),
            FallbackLockerDraftRegionLengthRule(80),
            FallbackLockerDraftNotesLengthRule(80),
        ),
    )
}
