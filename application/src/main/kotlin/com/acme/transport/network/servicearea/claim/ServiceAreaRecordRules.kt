package com.acme.transport.network.servicearea.claim

import com.acme.transport.kit.text.normalize

/** A rule a service area record must pass. Check returns its problems, or none. */
interface ServiceAreaRecordRule {
    fun check(item: ServiceAreaRecord): MutableList<String>
}

data class ServiceAreaRecordAllowedCodeRule(var allowed: MutableList<String>) : ServiceAreaRecordRule {
    override fun check(item: ServiceAreaRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaRecordRequiredChannelRule(var message: String) : ServiceAreaRecordRule {
    override fun check(item: ServiceAreaRecord): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaRecordTitleLengthRule(var maxLength: Int) : ServiceAreaRecordRule {
    override fun check(item: ServiceAreaRecord): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaRecordRequiredCodeRule(var message: String) : ServiceAreaRecordRule {
    override fun check(item: ServiceAreaRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaRecordRequiredTitleRule(var message: String) : ServiceAreaRecordRule {
    override fun check(item: ServiceAreaRecord): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area record. */
data class ServiceAreaRecordValidator(var rules: MutableList<ServiceAreaRecordRule>) {
    fun validate(item: ServiceAreaRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServiceAreaRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaRecordValidator(): ServiceAreaRecordValidator {
    return ServiceAreaRecordValidator(
        mutableListOf<ServiceAreaRecordRule>(
            ServiceAreaRecordAllowedCodeRule(mutableListOf<String>("seasonal", "legacy", "global")),
            ServiceAreaRecordRequiredChannelRule("service area record needs this field"),
            ServiceAreaRecordTitleLengthRule(1000),
            ServiceAreaRecordRequiredCodeRule("service area record needs a value"),
            ServiceAreaRecordRequiredTitleRule("service area record needs this field"),
        ),
    )
}
