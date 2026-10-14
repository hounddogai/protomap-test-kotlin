package com.acme.transport.customs.declaration.declarationforecast

/** A rule a tiered declaration summary must pass. Check returns its problems, or none. */
interface TieredDeclarationSummaryRule {
    fun check(item: TieredDeclarationSummary): MutableList<String>
}

data class TieredDeclarationSummaryCodePrefixRule(var prefix: String) : TieredDeclarationSummaryRule {
    override fun check(item: TieredDeclarationSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationSummaryOdometerRangeRule(
    var min: Int,
    var max: Int,
) : TieredDeclarationSummaryRule {
    override fun check(item: TieredDeclarationSummary): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationSummaryRequiredTrailerIdRule(var message: String) : TieredDeclarationSummaryRule {
    override fun check(item: TieredDeclarationSummary): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationSummaryRequiredLabelRule(var message: String) : TieredDeclarationSummaryRule {
    override fun check(item: TieredDeclarationSummary): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered declaration summary. */
data class TieredDeclarationSummaryValidator(var rules: MutableList<TieredDeclarationSummaryRule>) {
    fun validate(item: TieredDeclarationSummary): MutableList<String> {
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

    fun requireValid(item: TieredDeclarationSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDeclarationSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDeclarationSummaryValidator(): TieredDeclarationSummaryValidator {
    return TieredDeclarationSummaryValidator(
        mutableListOf<TieredDeclarationSummaryRule>(
            TieredDeclarationSummaryCodePrefixRule("ACM"),
            TieredDeclarationSummaryOdometerRangeRule(16, 255),
            TieredDeclarationSummaryRequiredTrailerIdRule("tiered declaration summary needs this field"),
            TieredDeclarationSummaryRequiredLabelRule("tiered declaration summary needs a value"),
        ),
    )
}
