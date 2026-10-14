package com.acme.transport.crewing.swap.request

/** A rule a scheduled swap request must pass. Check returns its problems, or none. */
interface ScheduledSwapRequestRule {
    fun check(item: ScheduledSwapRequest): MutableList<String>
}

data class ScheduledSwapRequestRequiredNameRule(var message: String) : ScheduledSwapRequestRule {
    override fun check(item: ScheduledSwapRequest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledSwapRequestRequiredCodeRule(var message: String) : ScheduledSwapRequestRule {
    override fun check(item: ScheduledSwapRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledSwapRequestRequiredChannelRule(var message: String) : ScheduledSwapRequestRule {
    override fun check(item: ScheduledSwapRequest): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled swap request. */
data class ScheduledSwapRequestValidator(var rules: MutableList<ScheduledSwapRequestRule>) {
    fun validate(item: ScheduledSwapRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledSwapRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledSwapRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledSwapRequestValidator(): ScheduledSwapRequestValidator {
    return ScheduledSwapRequestValidator(
        mutableListOf<ScheduledSwapRequestRule>(
            ScheduledSwapRequestRequiredNameRule("scheduled swap request needs a value"),
            ScheduledSwapRequestRequiredCodeRule("scheduled swap request needs this field"),
            ScheduledSwapRequestRequiredChannelRule("scheduled swap request needs this field"),
        ),
    )
}
