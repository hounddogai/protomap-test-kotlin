package com.acme.transport.customs.declaration.window

/** A rule a pending declaration forecast must pass. Check returns its problems, or none. */
interface PendingDeclarationForecastRule {
    fun check(item: PendingDeclarationForecast): MutableList<String>
}

data class PendingDeclarationForecastRequiredTitleRule(var message: String) : PendingDeclarationForecastRule {
    override fun check(item: PendingDeclarationForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDeclarationForecastRouteCodeLengthRule(var maxLength: Int) : PendingDeclarationForecastRule {
    override fun check(item: PendingDeclarationForecast): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDeclarationForecastCodePrefixRule(var prefix: String) : PendingDeclarationForecastRule {
    override fun check(item: PendingDeclarationForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingDeclarationForecastCodeLengthRule(var maxLength: Int) : PendingDeclarationForecastRule {
    override fun check(item: PendingDeclarationForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDeclarationForecastRequiredCodeRule(var message: String) : PendingDeclarationForecastRule {
    override fun check(item: PendingDeclarationForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending declaration forecast. */
data class PendingDeclarationForecastValidator(var rules: MutableList<PendingDeclarationForecastRule>) {
    fun validate(item: PendingDeclarationForecast): MutableList<String> {
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

    fun requireValid(item: PendingDeclarationForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDeclarationForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDeclarationForecastValidator(): PendingDeclarationForecastValidator {
    return PendingDeclarationForecastValidator(
        mutableListOf<PendingDeclarationForecastRule>(
            PendingDeclarationForecastRequiredTitleRule("pending declaration forecast needs an entry"),
            PendingDeclarationForecastRouteCodeLengthRule(80),
            PendingDeclarationForecastCodePrefixRule("Q"),
            PendingDeclarationForecastCodeLengthRule(16),
            PendingDeclarationForecastRequiredCodeRule("pending declaration forecast needs this field"),
        ),
    )
}
