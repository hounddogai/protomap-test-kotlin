package com.acme.transport.network.zone.policy

/** A rule a zone policy must pass. Check returns its problems, or none. */
interface ZonePolicyRule {
    fun check(item: ZonePolicy): MutableList<String>
}

data class ZonePolicyCodePrefixRule(var prefix: String) : ZonePolicyRule {
    override fun check(item: ZonePolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ZonePolicyRequiredCodeRule(var message: String) : ZonePolicyRule {
    override fun check(item: ZonePolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZonePolicyRequiredCurrencyRule(var message: String) : ZonePolicyRule {
    override fun check(item: ZonePolicy): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZonePolicyCodeLengthRule(var maxLength: Int) : ZonePolicyRule {
    override fun check(item: ZonePolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone policy. */
data class ZonePolicyValidator(var rules: MutableList<ZonePolicyRule>) {
    fun validate(item: ZonePolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ZonePolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZonePolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultZonePolicyValidator(): ZonePolicyValidator {
    return ZonePolicyValidator(
        mutableListOf<ZonePolicyRule>(
            ZonePolicyCodePrefixRule("OPS"),
            ZonePolicyRequiredCodeRule("zone policy needs a value"),
            ZonePolicyRequiredCurrencyRule("zone policy needs this field"),
            ZonePolicyCodeLengthRule(120),
        ),
    )
}
