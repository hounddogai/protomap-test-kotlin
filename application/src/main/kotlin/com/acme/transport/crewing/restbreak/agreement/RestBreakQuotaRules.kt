package com.acme.transport.crewing.restbreak.agreement

/** A rule a rest break quota must pass. Check returns its problems, or none. */
interface RestBreakQuotaRule {
    fun check(item: RestBreakQuota): MutableList<String>
}

data class RestBreakQuotaBatchSizeRangeRule(var min: Int, var max: Int) : RestBreakQuotaRule {
    override fun check(item: RestBreakQuota): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class RestBreakQuotaCodeLengthRule(var maxLength: Int) : RestBreakQuotaRule {
    override fun check(item: RestBreakQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakQuotaRequiredChannelRule(var message: String) : RestBreakQuotaRule {
    override fun check(item: RestBreakQuota): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakQuotaRequiredLabelRule(var message: String) : RestBreakQuotaRule {
    override fun check(item: RestBreakQuota): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break quota. */
data class RestBreakQuotaValidator(var rules: MutableList<RestBreakQuotaRule>) {
    fun validate(item: RestBreakQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RestBreakQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakQuotaValidator(): RestBreakQuotaValidator {
    return RestBreakQuotaValidator(
        mutableListOf<RestBreakQuotaRule>(
            RestBreakQuotaBatchSizeRangeRule(1000, 1000),
            RestBreakQuotaCodeLengthRule(64),
            RestBreakQuotaRequiredChannelRule("rest break quota needs this field"),
            RestBreakQuotaRequiredLabelRule("rest break quota needs an entry"),
        ),
    )
}
