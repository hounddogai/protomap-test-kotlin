package com.acme.transport.lastmile.drop.entry

/** A rule a drop quota must pass. Check returns its problems, or none. */
interface DropQuotaRule {
    fun check(item: DropQuota): MutableList<String>
}

data class DropQuotaTrailerIdLengthRule(var maxLength: Int) : DropQuotaRule {
    override fun check(item: DropQuota): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropQuotaNotesLengthRule(var maxLength: Int) : DropQuotaRule {
    override fun check(item: DropQuota): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropQuotaRequiredTrailerIdRule(var message: String) : DropQuotaRule {
    override fun check(item: DropQuota): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropQuotaCodeLengthRule(var maxLength: Int) : DropQuotaRule {
    override fun check(item: DropQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropQuotaVisibleGuardRule(var limit: Double) : DropQuotaRule {
    override fun check(item: DropQuota): MutableList<String> {
        if (item.visible && item.ratio > limit) {
            return mutableListOf<String>("ratio ${item.ratio} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class DropQuotaRequiredCodeRule(var message: String) : DropQuotaRule {
    override fun check(item: DropQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop quota. */
data class DropQuotaValidator(var rules: MutableList<DropQuotaRule>) {
    fun validate(item: DropQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DropQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultDropQuotaValidator(): DropQuotaValidator {
    return DropQuotaValidator(
        mutableListOf<DropQuotaRule>(
            DropQuotaTrailerIdLengthRule(1000),
            DropQuotaNotesLengthRule(120),
            DropQuotaRequiredTrailerIdRule("drop quota needs this field"),
            DropQuotaCodeLengthRule(80),
            DropQuotaVisibleGuardRule(1.4),
            DropQuotaRequiredCodeRule("drop quota needs a value"),
        ),
    )
}
