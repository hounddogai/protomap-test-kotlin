package com.acme.transport.network.servicearea.primaryareatarget

/** A rule a strict service area quota must pass. Check returns its problems, or none. */
interface StrictServiceAreaQuotaRule {
    fun check(item: StrictServiceAreaQuota): MutableList<String>
}

data class StrictServiceAreaQuotaRequiredContactEmailRule(var message: String) : StrictServiceAreaQuotaRule {
    override fun check(item: StrictServiceAreaQuota): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictServiceAreaQuotaContactEmailLengthRule(var maxLength: Int) : StrictServiceAreaQuotaRule {
    override fun check(item: StrictServiceAreaQuota): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictServiceAreaQuotaNameLengthRule(var maxLength: Int) : StrictServiceAreaQuotaRule {
    override fun check(item: StrictServiceAreaQuota): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictServiceAreaQuotaRequiredCodeRule(var message: String) : StrictServiceAreaQuotaRule {
    override fun check(item: StrictServiceAreaQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict service area quota. */
data class StrictServiceAreaQuotaValidator(var rules: MutableList<StrictServiceAreaQuotaRule>) {
    fun validate(item: StrictServiceAreaQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictServiceAreaQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictServiceAreaQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictServiceAreaQuotaValidator(): StrictServiceAreaQuotaValidator {
    return StrictServiceAreaQuotaValidator(
        mutableListOf<StrictServiceAreaQuotaRule>(
            StrictServiceAreaQuotaRequiredContactEmailRule("strict service area quota needs this field"),
            StrictServiceAreaQuotaContactEmailLengthRule(64),
            StrictServiceAreaQuotaNameLengthRule(1000),
            StrictServiceAreaQuotaRequiredCodeRule("strict service area quota needs a value"),
        ),
    )
}
