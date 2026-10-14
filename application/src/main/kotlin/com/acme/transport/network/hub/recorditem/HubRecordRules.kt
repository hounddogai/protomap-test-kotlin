package com.acme.transport.network.hub.recorditem

/** A rule a hub record must pass. Check returns its problems, or none. */
interface HubRecordRule {
    fun check(item: HubRecord): MutableList<String>
}

data class HubRecordRequiredCodeRule(var message: String) : HubRecordRule {
    override fun check(item: HubRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubRecordRequiredTimezoneRule(var message: String) : HubRecordRule {
    override fun check(item: HubRecord): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubRecordCodeLengthRule(var maxLength: Int) : HubRecordRule {
    override fun check(item: HubRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubRecordLabelLengthRule(var maxLength: Int) : HubRecordRule {
    override fun check(item: HubRecord): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubRecordRequiredLabelRule(var message: String) : HubRecordRule {
    override fun check(item: HubRecord): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubRecordRequiredChannelRule(var message: String) : HubRecordRule {
    override fun check(item: HubRecord): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub record. */
data class HubRecordValidator(var rules: MutableList<HubRecordRule>) {
    fun validate(item: HubRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HubRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultHubRecordValidator(): HubRecordValidator {
    return HubRecordValidator(
        mutableListOf<HubRecordRule>(
            HubRecordRequiredCodeRule("hub record needs a value"),
            HubRecordRequiredTimezoneRule("hub record needs a value"),
            HubRecordCodeLengthRule(3600),
            HubRecordLabelLengthRule(32),
            HubRecordRequiredLabelRule("hub record needs this field"),
            HubRecordRequiredChannelRule("hub record needs a value"),
        ),
    )
}
