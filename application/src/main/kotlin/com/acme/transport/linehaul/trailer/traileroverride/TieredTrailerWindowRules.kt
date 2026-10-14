package com.acme.transport.linehaul.trailer.traileroverride

/** A rule a tiered trailer window must pass. Check returns its problems, or none. */
interface TieredTrailerWindowRule {
    fun check(item: TieredTrailerWindow): MutableList<String>
}

data class TieredTrailerWindowRequiredLabelRule(var message: String) : TieredTrailerWindowRule {
    override fun check(item: TieredTrailerWindow): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTrailerWindowLabelLengthRule(var maxLength: Int) : TieredTrailerWindowRule {
    override fun check(item: TieredTrailerWindow): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredTrailerWindowCodePrefixRule(var prefix: String) : TieredTrailerWindowRule {
    override fun check(item: TieredTrailerWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredTrailerWindowRequiredShiftCodeRule(var message: String) : TieredTrailerWindowRule {
    override fun check(item: TieredTrailerWindow): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTrailerWindowShiftCodeLengthRule(var maxLength: Int) : TieredTrailerWindowRule {
    override fun check(item: TieredTrailerWindow): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered trailer window. */
data class TieredTrailerWindowValidator(var rules: MutableList<TieredTrailerWindowRule>) {
    fun validate(item: TieredTrailerWindow): MutableList<String> {
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

    fun requireValid(item: TieredTrailerWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredTrailerWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredTrailerWindowValidator(): TieredTrailerWindowValidator {
    return TieredTrailerWindowValidator(
        mutableListOf<TieredTrailerWindowRule>(
            TieredTrailerWindowRequiredLabelRule("tiered trailer window needs an entry"),
            TieredTrailerWindowLabelLengthRule(86400),
            TieredTrailerWindowCodePrefixRule("Q"),
            TieredTrailerWindowRequiredShiftCodeRule("tiered trailer window needs this field"),
            TieredTrailerWindowShiftCodeLengthRule(120),
        ),
    )
}
