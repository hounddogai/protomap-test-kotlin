package com.acme.transport.customs.clearance.request

/** A rule a default clearance notice must pass. Check returns its problems, or none. */
interface DefaultClearanceNoticeRule {
    fun check(item: DefaultClearanceNotice): MutableList<String>
}

data class DefaultClearanceNoticeRequiredTitleRule(var message: String) : DefaultClearanceNoticeRule {
    override fun check(item: DefaultClearanceNotice): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultClearanceNoticeRequiredCarrierRule(var message: String) : DefaultClearanceNoticeRule {
    override fun check(item: DefaultClearanceNotice): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultClearanceNoticeRequiredDescriptionRule(var message: String) : DefaultClearanceNoticeRule {
    override fun check(item: DefaultClearanceNotice): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default clearance notice. */
data class DefaultClearanceNoticeValidator(var rules: MutableList<DefaultClearanceNoticeRule>) {
    fun validate(item: DefaultClearanceNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultClearanceNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultClearanceNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultClearanceNoticeValidator(): DefaultClearanceNoticeValidator {
    return DefaultClearanceNoticeValidator(
        mutableListOf<DefaultClearanceNoticeRule>(
            DefaultClearanceNoticeRequiredTitleRule("default clearance notice needs this field"),
            DefaultClearanceNoticeRequiredCarrierRule("default clearance notice needs a value"),
            DefaultClearanceNoticeRequiredDescriptionRule("default clearance notice needs an entry"),
        ),
    )
}
