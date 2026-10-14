package com.acme.transport.lastmile.parcel.parcelbaseline

import com.acme.transport.kit.text.normalize

/** A rule a tiered parcel baseline must pass. Check returns its problems, or none. */
interface TieredParcelBaselineRule {
    fun check(item: TieredParcelBaseline): MutableList<String>
}

data class TieredParcelBaselineRequiredTitleRule(var message: String) : TieredParcelBaselineRule {
    override fun check(item: TieredParcelBaseline): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredParcelBaselineAllowedCodeRule(var allowed: MutableList<String>) : TieredParcelBaselineRule {
    override fun check(item: TieredParcelBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class TieredParcelBaselineTitleLengthRule(var maxLength: Int) : TieredParcelBaselineRule {
    override fun check(item: TieredParcelBaseline): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredParcelBaselineCodePrefixRule(var prefix: String) : TieredParcelBaselineRule {
    override fun check(item: TieredParcelBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredParcelBaselineRequiredShiftCodeRule(var message: String) : TieredParcelBaselineRule {
    override fun check(item: TieredParcelBaseline): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered parcel baseline. */
data class TieredParcelBaselineValidator(var rules: MutableList<TieredParcelBaselineRule>) {
    fun validate(item: TieredParcelBaseline): MutableList<String> {
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

    fun requireValid(item: TieredParcelBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredParcelBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredParcelBaselineValidator(): TieredParcelBaselineValidator {
    return TieredParcelBaselineValidator(
        mutableListOf<TieredParcelBaselineRule>(
            TieredParcelBaselineRequiredTitleRule("tiered parcel baseline needs this field"),
            TieredParcelBaselineAllowedCodeRule(mutableListOf<String>("regional")),
            TieredParcelBaselineTitleLengthRule(3600),
            TieredParcelBaselineCodePrefixRule("Q"),
            TieredParcelBaselineRequiredShiftCodeRule("tiered parcel baseline needs a value"),
        ),
    )
}
