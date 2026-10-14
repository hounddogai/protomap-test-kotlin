package com.acme.transport.safety.incident.bundle

/** A rule a primary incident quota must pass. Check returns its problems, or none. */
interface PrimaryIncidentQuotaRule {
    fun check(item: PrimaryIncidentQuota): MutableList<String>
}

data class PrimaryIncidentQuotaRequiredNameRule(var message: String) : PrimaryIncidentQuotaRule {
    override fun check(item: PrimaryIncidentQuota): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIncidentQuotaExternalRefLengthRule(var maxLength: Int) : PrimaryIncidentQuotaRule {
    override fun check(item: PrimaryIncidentQuota): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIncidentQuotaRequiredCodeRule(var message: String) : PrimaryIncidentQuotaRule {
    override fun check(item: PrimaryIncidentQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIncidentQuotaCodeLengthRule(var maxLength: Int) : PrimaryIncidentQuotaRule {
    override fun check(item: PrimaryIncidentQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIncidentQuotaRequiredExternalRefRule(var message: String) : PrimaryIncidentQuotaRule {
    override fun check(item: PrimaryIncidentQuota): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary incident quota. */
data class PrimaryIncidentQuotaValidator(var rules: MutableList<PrimaryIncidentQuotaRule>) {
    fun validate(item: PrimaryIncidentQuota): MutableList<String> {
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

    fun requireValid(item: PrimaryIncidentQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryIncidentQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryIncidentQuotaValidator(): PrimaryIncidentQuotaValidator {
    return PrimaryIncidentQuotaValidator(
        mutableListOf<PrimaryIncidentQuotaRule>(
            PrimaryIncidentQuotaRequiredNameRule("primary incident quota needs a value"),
            PrimaryIncidentQuotaExternalRefLengthRule(1000),
            PrimaryIncidentQuotaRequiredCodeRule("primary incident quota needs an entry"),
            PrimaryIncidentQuotaCodeLengthRule(64),
            PrimaryIncidentQuotaRequiredExternalRefRule("primary incident quota needs a value"),
        ),
    )
}
