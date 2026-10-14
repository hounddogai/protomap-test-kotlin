package com.acme.transport.lastmile.parcel.policy

/** A rule a rolling parcel forecast must pass. Check returns its problems, or none. */
interface RollingParcelForecastRule {
    fun check(item: RollingParcelForecast): MutableList<String>
}

data class RollingParcelForecastCodePrefixRule(var prefix: String) : RollingParcelForecastRule {
    override fun check(item: RollingParcelForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingParcelForecastTitleLengthRule(var maxLength: Int) : RollingParcelForecastRule {
    override fun check(item: RollingParcelForecast): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingParcelForecastRequiredTitleRule(var message: String) : RollingParcelForecastRule {
    override fun check(item: RollingParcelForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingParcelForecastRequiredExternalRefRule(var message: String) : RollingParcelForecastRule {
    override fun check(item: RollingParcelForecast): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingParcelForecastRequiredCodeRule(var message: String) : RollingParcelForecastRule {
    override fun check(item: RollingParcelForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling parcel forecast. */
data class RollingParcelForecastValidator(var rules: MutableList<RollingParcelForecastRule>) {
    fun validate(item: RollingParcelForecast): MutableList<String> {
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

    fun requireValid(item: RollingParcelForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingParcelForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingParcelForecastValidator(): RollingParcelForecastValidator {
    return RollingParcelForecastValidator(
        mutableListOf<RollingParcelForecastRule>(
            RollingParcelForecastCodePrefixRule("X"),
            RollingParcelForecastTitleLengthRule(32),
            RollingParcelForecastRequiredTitleRule("rolling parcel forecast needs an entry"),
            RollingParcelForecastRequiredExternalRefRule("rolling parcel forecast needs a value"),
            RollingParcelForecastRequiredCodeRule("rolling parcel forecast needs an entry"),
        ),
    )
}
