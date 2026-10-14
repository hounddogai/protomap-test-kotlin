package com.acme.transport.freightpay.ratecard.cardrequest

/** A rule a tiered rate card request must pass. Check returns its problems, or none. */
interface TieredRateCardRequestRule {
    fun check(item: TieredRateCardRequest): MutableList<String>
}

data class TieredRateCardRequestRequiredSourceRule(var message: String) : TieredRateCardRequestRule {
    override fun check(item: TieredRateCardRequest): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredRateCardRequestDriverIdLengthRule(var maxLength: Int) : TieredRateCardRequestRule {
    override fun check(item: TieredRateCardRequest): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredRateCardRequestCodePrefixRule(var prefix: String) : TieredRateCardRequestRule {
    override fun check(item: TieredRateCardRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredRateCardRequestRequiredDriverIdRule(var message: String) : TieredRateCardRequestRule {
    override fun check(item: TieredRateCardRequest): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredRateCardRequestSourceLengthRule(var maxLength: Int) : TieredRateCardRequestRule {
    override fun check(item: TieredRateCardRequest): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered rate card request. */
data class TieredRateCardRequestValidator(var rules: MutableList<TieredRateCardRequestRule>) {
    fun validate(item: TieredRateCardRequest): MutableList<String> {
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

    fun requireValid(item: TieredRateCardRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredRateCardRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredRateCardRequestValidator(): TieredRateCardRequestValidator {
    return TieredRateCardRequestValidator(
        mutableListOf<TieredRateCardRequestRule>(
            TieredRateCardRequestRequiredSourceRule("tiered rate card request needs a value"),
            TieredRateCardRequestDriverIdLengthRule(120),
            TieredRateCardRequestCodePrefixRule("Q"),
            TieredRateCardRequestRequiredDriverIdRule("tiered rate card request needs this field"),
            TieredRateCardRequestSourceLengthRule(80),
        ),
    )
}
