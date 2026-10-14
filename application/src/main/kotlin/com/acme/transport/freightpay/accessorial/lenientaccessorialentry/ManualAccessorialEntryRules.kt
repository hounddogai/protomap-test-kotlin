package com.acme.transport.freightpay.accessorial.lenientaccessorialentry

import com.acme.transport.kit.text.normalize

/** A rule a manual accessorial entry must pass. Check returns its problems, or none. */
interface ManualAccessorialEntryRule {
    fun check(item: ManualAccessorialEntry): MutableList<String>
}

data class ManualAccessorialEntryEtaMinutesRangeRule(var min: Int, var max: Int) : ManualAccessorialEntryRule {
    override fun check(item: ManualAccessorialEntry): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class ManualAccessorialEntryAllowedCodeRule(
    var allowed: MutableList<String>,
) : ManualAccessorialEntryRule {
    override fun check(item: ManualAccessorialEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class ManualAccessorialEntryCodePrefixRule(var prefix: String) : ManualAccessorialEntryRule {
    override fun check(item: ManualAccessorialEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualAccessorialEntryRequiredCodeRule(var message: String) : ManualAccessorialEntryRule {
    override fun check(item: ManualAccessorialEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualAccessorialEntryRequiredLabelRule(var message: String) : ManualAccessorialEntryRule {
    override fun check(item: ManualAccessorialEntry): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual accessorial entry. */
data class ManualAccessorialEntryValidator(var rules: MutableList<ManualAccessorialEntryRule>) {
    fun validate(item: ManualAccessorialEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualAccessorialEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualAccessorialEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultManualAccessorialEntryValidator(): ManualAccessorialEntryValidator {
    return ManualAccessorialEntryValidator(
        mutableListOf<ManualAccessorialEntryRule>(
            ManualAccessorialEntryEtaMinutesRangeRule(80, 3600),
            ManualAccessorialEntryAllowedCodeRule(mutableListOf<String>("seasonal", "tiered")),
            ManualAccessorialEntryCodePrefixRule("X"),
            ManualAccessorialEntryRequiredCodeRule("manual accessorial entry needs this field"),
            ManualAccessorialEntryRequiredLabelRule("manual accessorial entry needs an entry"),
        ),
    )
}
