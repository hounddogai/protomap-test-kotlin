package com.acme.transport.linehaul.relay.claim

/** A rule a fallback relay claim must pass. Check returns its problems, or none. */
interface FallbackRelayClaimRule {
    fun check(item: FallbackRelayClaim): MutableList<String>
}

data class FallbackRelayClaimRequiredNameRule(var message: String) : FallbackRelayClaimRule {
    override fun check(item: FallbackRelayClaim): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRelayClaimTitleLengthRule(var maxLength: Int) : FallbackRelayClaimRule {
    override fun check(item: FallbackRelayClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRelayClaimNameLengthRule(var maxLength: Int) : FallbackRelayClaimRule {
    override fun check(item: FallbackRelayClaim): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRelayClaimGraceDaysRangeRule(var min: Int, var max: Int) : FallbackRelayClaimRule {
    override fun check(item: FallbackRelayClaim): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class FallbackRelayClaimPhoneLengthRule(var maxLength: Int) : FallbackRelayClaimRule {
    override fun check(item: FallbackRelayClaim): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRelayClaimRequiredCodeRule(var message: String) : FallbackRelayClaimRule {
    override fun check(item: FallbackRelayClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback relay claim. */
data class FallbackRelayClaimValidator(var rules: MutableList<FallbackRelayClaimRule>) {
    fun validate(item: FallbackRelayClaim): MutableList<String> {
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

    fun requireValid(item: FallbackRelayClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRelayClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRelayClaimValidator(): FallbackRelayClaimValidator {
    return FallbackRelayClaimValidator(
        mutableListOf<FallbackRelayClaimRule>(
            FallbackRelayClaimRequiredNameRule("fallback relay claim needs a value"),
            FallbackRelayClaimTitleLengthRule(16),
            FallbackRelayClaimNameLengthRule(64),
            FallbackRelayClaimGraceDaysRangeRule(1000, 3600),
            FallbackRelayClaimPhoneLengthRule(1000),
            FallbackRelayClaimRequiredCodeRule("fallback relay claim needs a value"),
        ),
    )
}
