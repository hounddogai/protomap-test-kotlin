package com.acme.transport.linehaul.backhaul.backhaulquota

/** A rule a capped backhaul window must pass. Check returns its problems, or none. */
interface CappedBackhaulWindowRule {
    fun check(item: CappedBackhaulWindow): MutableList<String>
}

data class CappedBackhaulWindowRequiredCountryRule(var message: String) : CappedBackhaulWindowRule {
    override fun check(item: CappedBackhaulWindow): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedBackhaulWindowCountryLengthRule(var maxLength: Int) : CappedBackhaulWindowRule {
    override fun check(item: CappedBackhaulWindow): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedBackhaulWindowChannelLengthRule(var maxLength: Int) : CappedBackhaulWindowRule {
    override fun check(item: CappedBackhaulWindow): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped backhaul window. */
data class CappedBackhaulWindowValidator(var rules: MutableList<CappedBackhaulWindowRule>) {
    fun validate(item: CappedBackhaulWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedBackhaulWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedBackhaulWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedBackhaulWindowValidator(): CappedBackhaulWindowValidator {
    return CappedBackhaulWindowValidator(
        mutableListOf<CappedBackhaulWindowRule>(
            CappedBackhaulWindowRequiredCountryRule("capped backhaul window needs an entry"),
            CappedBackhaulWindowCountryLengthRule(32),
            CappedBackhaulWindowChannelLengthRule(1000),
        ),
    )
}
