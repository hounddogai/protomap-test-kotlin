package com.acme.transport.linehaul.capacity.segment

/** A rule a global capacity agreement must pass. Check returns its problems, or none. */
interface GlobalCapacityAgreementRule {
    fun check(item: GlobalCapacityAgreement): MutableList<String>
}

data class GlobalCapacityAgreementCodePrefixRule(var prefix: String) : GlobalCapacityAgreementRule {
    override fun check(item: GlobalCapacityAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCapacityAgreementTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : GlobalCapacityAgreementRule {
    override fun check(item: GlobalCapacityAgreement): MutableList<String> {
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

data class GlobalCapacityAgreementTitleLengthRule(var maxLength: Int) : GlobalCapacityAgreementRule {
    override fun check(item: GlobalCapacityAgreement): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global capacity agreement. */
data class GlobalCapacityAgreementValidator(var rules: MutableList<GlobalCapacityAgreementRule>) {
    fun validate(item: GlobalCapacityAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalCapacityAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalCapacityAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalCapacityAgreementValidator(): GlobalCapacityAgreementValidator {
    return GlobalCapacityAgreementValidator(
        mutableListOf<GlobalCapacityAgreementRule>(
            GlobalCapacityAgreementCodePrefixRule("ACM"),
            GlobalCapacityAgreementTagsLimitRule(16, mutableListOf<String>()),
            GlobalCapacityAgreementTitleLengthRule(16),
        ),
    )
}
