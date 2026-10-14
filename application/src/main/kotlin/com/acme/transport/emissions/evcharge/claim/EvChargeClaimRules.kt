package com.acme.transport.emissions.evcharge.claim

/** A rule a ev charge claim must pass. Check returns its problems, or none. */
interface EvChargeClaimRule {
    fun check(item: EvChargeClaim): MutableList<String>
}

data class EvChargeClaimCountryLengthRule(var maxLength: Int) : EvChargeClaimRule {
    override fun check(item: EvChargeClaim): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeClaimCodeLengthRule(var maxLength: Int) : EvChargeClaimRule {
    override fun check(item: EvChargeClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeClaimRequiredChannelRule(var message: String) : EvChargeClaimRule {
    override fun check(item: EvChargeClaim): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeClaimTitleLengthRule(var maxLength: Int) : EvChargeClaimRule {
    override fun check(item: EvChargeClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeClaimRequiredTitleRule(var message: String) : EvChargeClaimRule {
    override fun check(item: EvChargeClaim): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge claim. */
data class EvChargeClaimValidator(var rules: MutableList<EvChargeClaimRule>) {
    fun validate(item: EvChargeClaim): MutableList<String> {
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

    fun requireValid(item: EvChargeClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeClaimValidator(): EvChargeClaimValidator {
    return EvChargeClaimValidator(
        mutableListOf<EvChargeClaimRule>(
            EvChargeClaimCountryLengthRule(80),
            EvChargeClaimCodeLengthRule(16),
            EvChargeClaimRequiredChannelRule("ev charge claim needs an entry"),
            EvChargeClaimTitleLengthRule(80),
            EvChargeClaimRequiredTitleRule("ev charge claim needs a value"),
        ),
    )
}
