package com.acme.transport.network.hub.notice

/** A rule a regional hub agreement must pass. Check returns its problems, or none. */
interface RegionalHubAgreementRule {
    fun check(item: RegionalHubAgreement): MutableList<String>
}

data class RegionalHubAgreementNameLengthRule(var maxLength: Int) : RegionalHubAgreementRule {
    override fun check(item: RegionalHubAgreement): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalHubAgreementChannelLengthRule(var maxLength: Int) : RegionalHubAgreementRule {
    override fun check(item: RegionalHubAgreement): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalHubAgreementCodePrefixRule(var prefix: String) : RegionalHubAgreementRule {
    override fun check(item: RegionalHubAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalHubAgreementRequiredNameRule(var message: String) : RegionalHubAgreementRule {
    override fun check(item: RegionalHubAgreement): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional hub agreement. */
data class RegionalHubAgreementValidator(var rules: MutableList<RegionalHubAgreementRule>) {
    fun validate(item: RegionalHubAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalHubAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalHubAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalHubAgreementValidator(): RegionalHubAgreementValidator {
    return RegionalHubAgreementValidator(
        mutableListOf<RegionalHubAgreementRule>(
            RegionalHubAgreementNameLengthRule(3600),
            RegionalHubAgreementChannelLengthRule(1000),
            RegionalHubAgreementCodePrefixRule("Q"),
            RegionalHubAgreementRequiredNameRule("regional hub agreement needs this field"),
        ),
    )
}
