package com.acme.transport.lastmile.redelivery.metric

/** A rule a default redelivery notice must pass. Check returns its problems, or none. */
interface DefaultRedeliveryNoticeRule {
    fun check(item: DefaultRedeliveryNotice): MutableList<String>
}

data class DefaultRedeliveryNoticeCodeLengthRule(var maxLength: Int) : DefaultRedeliveryNoticeRule {
    override fun check(item: DefaultRedeliveryNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRedeliveryNoticeAttemptsRangeRule(var min: Int, var max: Int) : DefaultRedeliveryNoticeRule {
    override fun check(item: DefaultRedeliveryNotice): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRedeliveryNoticeRetriesRangeRule(var min: Int, var max: Int) : DefaultRedeliveryNoticeRule {
    override fun check(item: DefaultRedeliveryNotice): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class DefaultRedeliveryNoticeRequiredOwnerIdRule(var message: String) : DefaultRedeliveryNoticeRule {
    override fun check(item: DefaultRedeliveryNotice): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default redelivery notice. */
data class DefaultRedeliveryNoticeValidator(var rules: MutableList<DefaultRedeliveryNoticeRule>) {
    fun validate(item: DefaultRedeliveryNotice): MutableList<String> {
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

    fun requireValid(item: DefaultRedeliveryNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRedeliveryNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRedeliveryNoticeValidator(): DefaultRedeliveryNoticeValidator {
    return DefaultRedeliveryNoticeValidator(
        mutableListOf<DefaultRedeliveryNoticeRule>(
            DefaultRedeliveryNoticeCodeLengthRule(32),
            DefaultRedeliveryNoticeAttemptsRangeRule(120, 1000),
            DefaultRedeliveryNoticeRetriesRangeRule(64, 120),
            DefaultRedeliveryNoticeRequiredOwnerIdRule("default redelivery notice needs a value"),
        ),
    )
}
