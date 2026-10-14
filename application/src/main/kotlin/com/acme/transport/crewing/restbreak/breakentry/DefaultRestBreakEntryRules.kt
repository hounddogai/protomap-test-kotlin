package com.acme.transport.crewing.restbreak.breakentry

import com.acme.transport.kit.text.normalize

/** A rule a default rest break entry must pass. Check returns its problems, or none. */
interface DefaultRestBreakEntryRule {
    fun check(item: DefaultRestBreakEntry): MutableList<String>
}

data class DefaultRestBreakEntryCodePrefixRule(var prefix: String) : DefaultRestBreakEntryRule {
    override fun check(item: DefaultRestBreakEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRestBreakEntryAllowedNotesRule(var allowed: MutableList<String>) : DefaultRestBreakEntryRule {
    override fun check(item: DefaultRestBreakEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRestBreakEntryDiscountRangeRule(
    var min: Double,
    var max: Double,
) : DefaultRestBreakEntryRule {
    override fun check(item: DefaultRestBreakEntry): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class DefaultRestBreakEntryRequiredCodeRule(var message: String) : DefaultRestBreakEntryRule {
    override fun check(item: DefaultRestBreakEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRestBreakEntryRequiredOwnerIdRule(var message: String) : DefaultRestBreakEntryRule {
    override fun check(item: DefaultRestBreakEntry): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default rest break entry. */
data class DefaultRestBreakEntryValidator(var rules: MutableList<DefaultRestBreakEntryRule>) {
    fun validate(item: DefaultRestBreakEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultRestBreakEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRestBreakEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRestBreakEntryValidator(): DefaultRestBreakEntryValidator {
    return DefaultRestBreakEntryValidator(
        mutableListOf<DefaultRestBreakEntryRule>(
            DefaultRestBreakEntryCodePrefixRule("OPS"),
            DefaultRestBreakEntryAllowedNotesRule(mutableListOf<String>()),
            DefaultRestBreakEntryDiscountRangeRule(8.0, 1538.9),
            DefaultRestBreakEntryRequiredCodeRule("default rest break entry needs this field"),
            DefaultRestBreakEntryRequiredOwnerIdRule("default rest break entry needs a value"),
        ),
    )
}
