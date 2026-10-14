package com.acme.transport.freightpay.dispute.agreement

/** A rule a dispute agreement must pass. Check returns its problems, or none. */
interface DisputeAgreementRule {
    fun check(item: DisputeAgreement): MutableList<String>
}

data class DisputeAgreementRequiredRouteCodeRule(var message: String) : DisputeAgreementRule {
    override fun check(item: DisputeAgreement): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeAgreementRouteCodeLengthRule(var maxLength: Int) : DisputeAgreementRule {
    override fun check(item: DisputeAgreement): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeAgreementCodePrefixRule(var prefix: String) : DisputeAgreementRule {
    override fun check(item: DisputeAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DisputeAgreementTitleLengthRule(var maxLength: Int) : DisputeAgreementRule {
    override fun check(item: DisputeAgreement): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeAgreementTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : DisputeAgreementRule {
    override fun check(item: DisputeAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

/** Runs every rule of a dispute agreement. */
data class DisputeAgreementValidator(var rules: MutableList<DisputeAgreementRule>) {
    fun validate(item: DisputeAgreement): MutableList<String> {
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

    fun requireValid(item: DisputeAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeAgreementValidator(): DisputeAgreementValidator {
    return DisputeAgreementValidator(
        mutableListOf<DisputeAgreementRule>(
            DisputeAgreementRequiredRouteCodeRule("dispute agreement needs an entry"),
            DisputeAgreementRouteCodeLengthRule(3600),
            DisputeAgreementCodePrefixRule("Q"),
            DisputeAgreementTitleLengthRule(255),
            DisputeAgreementTagsLimitRule(255, mutableListOf<String>()),
        ),
    )
}
