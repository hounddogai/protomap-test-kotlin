package com.acme.transport.network.zone.agreement

/** A rule a zone baseline must pass. Check returns its problems, or none. */
interface ZoneBaselineRule {
    fun check(item: ZoneBaseline): MutableList<String>
}

data class ZoneBaselineLabelLengthRule(var maxLength: Int) : ZoneBaselineRule {
    override fun check(item: ZoneBaseline): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneBaselineRequiredCodeRule(var message: String) : ZoneBaselineRule {
    override fun check(item: ZoneBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneBaselineCurrencyLengthRule(var maxLength: Int) : ZoneBaselineRule {
    override fun check(item: ZoneBaseline): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneBaselineLoadKgRangeRule(var min: Double, var max: Double) : ZoneBaselineRule {
    override fun check(item: ZoneBaseline): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone baseline. */
data class ZoneBaselineValidator(var rules: MutableList<ZoneBaselineRule>) {
    fun validate(item: ZoneBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ZoneBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneBaselineValidator(): ZoneBaselineValidator {
    return ZoneBaselineValidator(
        mutableListOf<ZoneBaselineRule>(
            ZoneBaselineLabelLengthRule(86400),
            ZoneBaselineRequiredCodeRule("zone baseline needs a value"),
            ZoneBaselineCurrencyLengthRule(3600),
            ZoneBaselineLoadKgRangeRule(3.3, 3577.8),
        ),
    )
}
