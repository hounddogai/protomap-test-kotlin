package com.acme.transport.network.hub.notice

/** A rule a hub notice must pass. Check returns its problems, or none. */
interface HubNoticeRule {
    fun check(item: HubNotice): MutableList<String>
}

data class HubNoticeFuelLitersRangeRule(var min: Double, var max: Double) : HubNoticeRule {
    override fun check(item: HubNotice): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HubNoticeCreatedAtRangeRule(var min: Int, var max: Int) : HubNoticeRule {
    override fun check(item: HubNotice): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class HubNoticeCodePrefixRule(var prefix: String) : HubNoticeRule {
    override fun check(item: HubNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class HubNoticeNameLengthRule(var maxLength: Int) : HubNoticeRule {
    override fun check(item: HubNotice): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub notice. */
data class HubNoticeValidator(var rules: MutableList<HubNoticeRule>) {
    fun validate(item: HubNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HubNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultHubNoticeValidator(): HubNoticeValidator {
    return HubNoticeValidator(
        mutableListOf<HubNoticeRule>(
            HubNoticeFuelLitersRangeRule(5.1, 3096.4),
            HubNoticeCreatedAtRangeRule(80, 1000),
            HubNoticeCodePrefixRule("REF"),
            HubNoticeNameLengthRule(3600),
        ),
    )
}
