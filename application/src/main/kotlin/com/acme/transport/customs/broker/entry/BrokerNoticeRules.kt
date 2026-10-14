package com.acme.transport.customs.broker.entry

/** A rule a broker notice must pass. Check returns its problems, or none. */
interface BrokerNoticeRule {
    fun check(item: BrokerNotice): MutableList<String>
}

data class BrokerNoticeLabelLengthRule(var maxLength: Int) : BrokerNoticeRule {
    override fun check(item: BrokerNotice): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerNoticeCodePrefixRule(var prefix: String) : BrokerNoticeRule {
    override fun check(item: BrokerNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BrokerNoticeDurationMinutesRangeRule(var min: Int, var max: Int) : BrokerNoticeRule {
    override fun check(item: BrokerNotice): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BrokerNoticeQuantityRangeRule(var min: Int, var max: Int) : BrokerNoticeRule {
    override fun check(item: BrokerNotice): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BrokerNoticeRequiredLabelRule(var message: String) : BrokerNoticeRule {
    override fun check(item: BrokerNotice): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerNoticeCodeLengthRule(var maxLength: Int) : BrokerNoticeRule {
    override fun check(item: BrokerNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker notice. */
data class BrokerNoticeValidator(var rules: MutableList<BrokerNoticeRule>) {
    fun validate(item: BrokerNotice): MutableList<String> {
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

    fun requireValid(item: BrokerNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerNoticeValidator(): BrokerNoticeValidator {
    return BrokerNoticeValidator(
        mutableListOf<BrokerNoticeRule>(
            BrokerNoticeLabelLengthRule(16),
            BrokerNoticeCodePrefixRule("ACM"),
            BrokerNoticeDurationMinutesRangeRule(16, 86400),
            BrokerNoticeQuantityRangeRule(64, 255),
            BrokerNoticeRequiredLabelRule("broker notice needs a value"),
            BrokerNoticeCodeLengthRule(80),
        ),
    )
}
