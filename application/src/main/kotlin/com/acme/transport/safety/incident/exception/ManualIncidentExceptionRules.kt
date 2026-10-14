package com.acme.transport.safety.incident.exception

/** A rule a manual incident exception must pass. Check returns its problems, or none. */
interface ManualIncidentExceptionRule {
    fun check(item: ManualIncidentException): MutableList<String>
}

data class ManualIncidentExceptionRequiredCodeRule(var message: String) : ManualIncidentExceptionRule {
    override fun check(item: ManualIncidentException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualIncidentExceptionExternalRefLengthRule(var maxLength: Int) : ManualIncidentExceptionRule {
    override fun check(item: ManualIncidentException): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualIncidentExceptionTitleLengthRule(var maxLength: Int) : ManualIncidentExceptionRule {
    override fun check(item: ManualIncidentException): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualIncidentExceptionRequiredExternalRefRule(var message: String) : ManualIncidentExceptionRule {
    override fun check(item: ManualIncidentException): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual incident exception. */
data class ManualIncidentExceptionValidator(var rules: MutableList<ManualIncidentExceptionRule>) {
    fun validate(item: ManualIncidentException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualIncidentException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualIncidentException>): MutableMap<String, MutableList<String>> {
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

fun defaultManualIncidentExceptionValidator(): ManualIncidentExceptionValidator {
    return ManualIncidentExceptionValidator(
        mutableListOf<ManualIncidentExceptionRule>(
            ManualIncidentExceptionRequiredCodeRule("manual incident exception needs an entry"),
            ManualIncidentExceptionExternalRefLengthRule(3600),
            ManualIncidentExceptionTitleLengthRule(86400),
            ManualIncidentExceptionRequiredExternalRefRule("manual incident exception needs an entry"),
        ),
    )
}
