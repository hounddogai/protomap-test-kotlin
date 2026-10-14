package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a zone group must pass. Check returns its problems, or none. */
interface ZoneGroupRule {
    fun check(item: ZoneGroup): MutableList<String>
}

data class ZoneGroupRequiredLabelRule(var message: String) : ZoneGroupRule {
    override fun check(item: ZoneGroup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneGroupRequiredTitleRule(var message: String) : ZoneGroupRule {
    override fun check(item: ZoneGroup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneGroupDiscountRangeRule(var min: Double, var max: Double) : ZoneGroupRule {
    override fun check(item: ZoneGroup): MutableList<String> {
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

data class ZoneGroupAllowedTitleRule(var allowed: MutableList<String>) : ZoneGroupRule {
    override fun check(item: ZoneGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone group. */
data class ZoneGroupValidator(var rules: MutableList<ZoneGroupRule>) {
    fun validate(item: ZoneGroup): MutableList<String> {
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

    fun requireValid(item: ZoneGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneGroupValidator(): ZoneGroupValidator {
    return ZoneGroupValidator(
        mutableListOf<ZoneGroupRule>(
            ZoneGroupRequiredLabelRule("zone group needs an entry"),
            ZoneGroupRequiredTitleRule("zone group needs this field"),
            ZoneGroupDiscountRangeRule(9.6, 108.6),
            ZoneGroupAllowedTitleRule(mutableListOf<String>("scheduled", "fallback")),
        ),
    )
}
