package com.acme.transport.lastmile.redelivery.redeliveryclaim

import com.acme.transport.kit.text.normalize

/** A rule a primary redelivery claim must pass. Check returns its problems, or none. */
interface PrimaryRedeliveryClaimRule {
    fun check(item: PrimaryRedeliveryClaim): MutableList<String>
}

data class PrimaryRedeliveryClaimAllowedChannelRule(
    var allowed: MutableList<String>,
) : PrimaryRedeliveryClaimRule {
    override fun check(item: PrimaryRedeliveryClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is expired")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRedeliveryClaimRequiredChannelRule(var message: String) : PrimaryRedeliveryClaimRule {
    override fun check(item: PrimaryRedeliveryClaim): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRedeliveryClaimRequiredLabelRule(var message: String) : PrimaryRedeliveryClaimRule {
    override fun check(item: PrimaryRedeliveryClaim): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRedeliveryClaimRequiredCodeRule(var message: String) : PrimaryRedeliveryClaimRule {
    override fun check(item: PrimaryRedeliveryClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRedeliveryClaimChannelLengthRule(var maxLength: Int) : PrimaryRedeliveryClaimRule {
    override fun check(item: PrimaryRedeliveryClaim): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary redelivery claim. */
data class PrimaryRedeliveryClaimValidator(var rules: MutableList<PrimaryRedeliveryClaimRule>) {
    fun validate(item: PrimaryRedeliveryClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryRedeliveryClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryRedeliveryClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryRedeliveryClaimValidator(): PrimaryRedeliveryClaimValidator {
    return PrimaryRedeliveryClaimValidator(
        mutableListOf<PrimaryRedeliveryClaimRule>(
            PrimaryRedeliveryClaimAllowedChannelRule(mutableListOf<String>()),
            PrimaryRedeliveryClaimRequiredChannelRule("primary redelivery claim needs an entry"),
            PrimaryRedeliveryClaimRequiredLabelRule("primary redelivery claim needs a value"),
            PrimaryRedeliveryClaimRequiredCodeRule("primary redelivery claim needs this field"),
            PrimaryRedeliveryClaimChannelLengthRule(3600),
        ),
    )
}
