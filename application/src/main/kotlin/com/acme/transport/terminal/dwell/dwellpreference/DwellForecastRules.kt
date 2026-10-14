package com.acme.transport.terminal.dwell.dwellpreference

/** A rule a dwell forecast must pass. Check returns its problems, or none. */
interface DwellForecastRule {
    fun check(item: DwellForecast): MutableList<String>
}

data class DwellForecastCountryLengthRule(var maxLength: Int) : DwellForecastRule {
    override fun check(item: DwellForecast): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellForecastNotesLengthRule(var maxLength: Int) : DwellForecastRule {
    override fun check(item: DwellForecast): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellForecastRequiredTitleRule(var message: String) : DwellForecastRule {
    override fun check(item: DwellForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellForecastRequiredNotesRule(var message: String) : DwellForecastRule {
    override fun check(item: DwellForecast): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellForecastRequiredCodeRule(var message: String) : DwellForecastRule {
    override fun check(item: DwellForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell forecast. */
data class DwellForecastValidator(var rules: MutableList<DwellForecastRule>) {
    fun validate(item: DwellForecast): MutableList<String> {
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

    fun requireValid(item: DwellForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellForecastValidator(): DwellForecastValidator {
    return DwellForecastValidator(
        mutableListOf<DwellForecastRule>(
            DwellForecastCountryLengthRule(86400),
            DwellForecastNotesLengthRule(120),
            DwellForecastRequiredTitleRule("dwell forecast needs this field"),
            DwellForecastRequiredNotesRule("dwell forecast needs this field"),
            DwellForecastRequiredCodeRule("dwell forecast needs a value"),
        ),
    )
}
