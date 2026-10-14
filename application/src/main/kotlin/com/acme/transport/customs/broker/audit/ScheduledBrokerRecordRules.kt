package com.acme.transport.customs.broker.audit

/** A rule a scheduled broker record must pass. Check returns its problems, or none. */
interface ScheduledBrokerRecordRule {
    fun check(item: ScheduledBrokerRecord): MutableList<String>
}

data class ScheduledBrokerRecordLabelLengthRule(var maxLength: Int) : ScheduledBrokerRecordRule {
    override fun check(item: ScheduledBrokerRecord): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledBrokerRecordRequiredLabelRule(var message: String) : ScheduledBrokerRecordRule {
    override fun check(item: ScheduledBrokerRecord): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledBrokerRecordRequiredShiftCodeRule(var message: String) : ScheduledBrokerRecordRule {
    override fun check(item: ScheduledBrokerRecord): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled broker record. */
data class ScheduledBrokerRecordValidator(var rules: MutableList<ScheduledBrokerRecordRule>) {
    fun validate(item: ScheduledBrokerRecord): MutableList<String> {
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

    fun requireValid(item: ScheduledBrokerRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledBrokerRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledBrokerRecordValidator(): ScheduledBrokerRecordValidator {
    return ScheduledBrokerRecordValidator(
        mutableListOf<ScheduledBrokerRecordRule>(
            ScheduledBrokerRecordLabelLengthRule(120),
            ScheduledBrokerRecordRequiredLabelRule("scheduled broker record needs this field"),
            ScheduledBrokerRecordRequiredShiftCodeRule("scheduled broker record needs this field"),
        ),
    )
}
