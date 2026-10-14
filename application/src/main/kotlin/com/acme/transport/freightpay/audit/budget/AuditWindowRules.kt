package com.acme.transport.freightpay.audit.budget

/** A rule a audit window must pass. Check returns its problems, or none. */
interface AuditWindowRule {
    fun check(item: AuditWindow): MutableList<String>
}

data class AuditWindowPinnedGuardRule(var limit: Double) : AuditWindowRule {
    override fun check(item: AuditWindow): MutableList<String> {
        if (item.pinned && item.budget > limit) {
            return mutableListOf<String>("budget ${item.budget} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class AuditWindowRequiredCodeRule(var message: String) : AuditWindowRule {
    override fun check(item: AuditWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditWindowTitleLengthRule(var maxLength: Int) : AuditWindowRule {
    override fun check(item: AuditWindow): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit window. */
data class AuditWindowValidator(var rules: MutableList<AuditWindowRule>) {
    fun validate(item: AuditWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditWindowValidator(): AuditWindowValidator {
    return AuditWindowValidator(
        mutableListOf<AuditWindowRule>(
            AuditWindowPinnedGuardRule(8.0),
            AuditWindowRequiredCodeRule("audit window needs a value"),
            AuditWindowTitleLengthRule(120),
        ),
    )
}
