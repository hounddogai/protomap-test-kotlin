package com.acme.transport.linehaul.laneplan.rule

/** A rule a capped lane plan policy must pass. Check returns its problems, or none. */
interface CappedLanePlanPolicyRule {
    fun check(item: CappedLanePlanPolicy): MutableList<String>
}

data class CappedLanePlanPolicySummaryLengthRule(var maxLength: Int) : CappedLanePlanPolicyRule {
    override fun check(item: CappedLanePlanPolicy): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedLanePlanPolicyNameLengthRule(var maxLength: Int) : CappedLanePlanPolicyRule {
    override fun check(item: CappedLanePlanPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedLanePlanPolicyCodeLengthRule(var maxLength: Int) : CappedLanePlanPolicyRule {
    override fun check(item: CappedLanePlanPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedLanePlanPolicyRequiredCodeRule(var message: String) : CappedLanePlanPolicyRule {
    override fun check(item: CappedLanePlanPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped lane plan policy. */
data class CappedLanePlanPolicyValidator(var rules: MutableList<CappedLanePlanPolicyRule>) {
    fun validate(item: CappedLanePlanPolicy): MutableList<String> {
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

    fun requireValid(item: CappedLanePlanPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedLanePlanPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedLanePlanPolicyValidator(): CappedLanePlanPolicyValidator {
    return CappedLanePlanPolicyValidator(
        mutableListOf<CappedLanePlanPolicyRule>(
            CappedLanePlanPolicySummaryLengthRule(16),
            CappedLanePlanPolicyNameLengthRule(86400),
            CappedLanePlanPolicyCodeLengthRule(1000),
            CappedLanePlanPolicyRequiredCodeRule("capped lane plan policy needs a value"),
        ),
    )
}
