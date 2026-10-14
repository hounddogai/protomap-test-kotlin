package com.acme.transport.safety.incident.incidentsnapshot

/** A rule a incident notice must pass. Check returns its problems, or none. */
interface IncidentNoticeRule {
    fun check(item: IncidentNotice): MutableList<String>
}

data class IncidentNoticeCurrencyLengthRule(var maxLength: Int) : IncidentNoticeRule {
    override fun check(item: IncidentNotice): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentNoticeNameLengthRule(var maxLength: Int) : IncidentNoticeRule {
    override fun check(item: IncidentNotice): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentNoticeCodeLengthRule(var maxLength: Int) : IncidentNoticeRule {
    override fun check(item: IncidentNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident notice. */
data class IncidentNoticeValidator(var rules: MutableList<IncidentNoticeRule>) {
    fun validate(item: IncidentNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IncidentNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentNoticeValidator(): IncidentNoticeValidator {
    return IncidentNoticeValidator(
        mutableListOf<IncidentNoticeRule>(
            IncidentNoticeCurrencyLengthRule(80),
            IncidentNoticeNameLengthRule(3600),
            IncidentNoticeCodeLengthRule(1000),
        ),
    )
}
