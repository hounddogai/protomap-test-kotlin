package com.acme.transport.network.zone.tier

/** A rule a zone milestone must pass. Check returns its problems, or none. */
interface ZoneMilestoneRule {
    fun check(item: ZoneMilestone): MutableList<String>
}

data class ZoneMilestoneRequiredTitleRule(var message: String) : ZoneMilestoneRule {
    override fun check(item: ZoneMilestone): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneMilestoneRequiredCurrencyRule(var message: String) : ZoneMilestoneRule {
    override fun check(item: ZoneMilestone): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneMilestoneRequiredChannelRule(var message: String) : ZoneMilestoneRule {
    override fun check(item: ZoneMilestone): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneMilestoneChannelLengthRule(var maxLength: Int) : ZoneMilestoneRule {
    override fun check(item: ZoneMilestone): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone milestone. */
data class ZoneMilestoneValidator(var rules: MutableList<ZoneMilestoneRule>) {
    fun validate(item: ZoneMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ZoneMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneMilestoneValidator(): ZoneMilestoneValidator {
    return ZoneMilestoneValidator(
        mutableListOf<ZoneMilestoneRule>(
            ZoneMilestoneRequiredTitleRule("zone milestone needs an entry"),
            ZoneMilestoneRequiredCurrencyRule("zone milestone needs a value"),
            ZoneMilestoneRequiredChannelRule("zone milestone needs a value"),
            ZoneMilestoneChannelLengthRule(255),
        ),
    )
}
