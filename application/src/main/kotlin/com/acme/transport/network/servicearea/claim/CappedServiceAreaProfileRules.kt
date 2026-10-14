package com.acme.transport.network.servicearea.claim

/** A rule a capped service area profile must pass. Check returns its problems, or none. */
interface CappedServiceAreaProfileRule {
    fun check(item: CappedServiceAreaProfile): MutableList<String>
}

data class CappedServiceAreaProfileRequiredCodeRule(var message: String) : CappedServiceAreaProfileRule {
    override fun check(item: CappedServiceAreaProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedServiceAreaProfileRequiredNameRule(var message: String) : CappedServiceAreaProfileRule {
    override fun check(item: CappedServiceAreaProfile): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedServiceAreaProfileRequiredChannelRule(var message: String) : CappedServiceAreaProfileRule {
    override fun check(item: CappedServiceAreaProfile): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped service area profile. */
data class CappedServiceAreaProfileValidator(var rules: MutableList<CappedServiceAreaProfileRule>) {
    fun validate(item: CappedServiceAreaProfile): MutableList<String> {
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

    fun requireValid(item: CappedServiceAreaProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedServiceAreaProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedServiceAreaProfileValidator(): CappedServiceAreaProfileValidator {
    return CappedServiceAreaProfileValidator(
        mutableListOf<CappedServiceAreaProfileRule>(
            CappedServiceAreaProfileRequiredCodeRule("capped service area profile needs an entry"),
            CappedServiceAreaProfileRequiredNameRule("capped service area profile needs an entry"),
            CappedServiceAreaProfileRequiredChannelRule("capped service area profile needs this field"),
        ),
    )
}
