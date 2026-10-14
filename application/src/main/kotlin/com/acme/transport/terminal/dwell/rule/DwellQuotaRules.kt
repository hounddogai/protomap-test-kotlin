package com.acme.transport.terminal.dwell.rule

/** A rule a dwell quota must pass. Check returns its problems, or none. */
interface DwellQuotaRule {
    fun check(item: DwellQuota): MutableList<String>
}

data class DwellQuotaRequiredCountryRule(var message: String) : DwellQuotaRule {
    override fun check(item: DwellQuota): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellQuotaRequiredLabelRule(var message: String) : DwellQuotaRule {
    override fun check(item: DwellQuota): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellQuotaMarginRangeRule(var min: Double, var max: Double) : DwellQuotaRule {
    override fun check(item: DwellQuota): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DwellQuotaLabelLengthRule(var maxLength: Int) : DwellQuotaRule {
    override fun check(item: DwellQuota): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell quota. */
data class DwellQuotaValidator(var rules: MutableList<DwellQuotaRule>) {
    fun validate(item: DwellQuota): MutableList<String> {
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

    fun requireValid(item: DwellQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellQuotaValidator(): DwellQuotaValidator {
    return DwellQuotaValidator(
        mutableListOf<DwellQuotaRule>(
            DwellQuotaRequiredCountryRule("dwell quota needs a value"),
            DwellQuotaRequiredLabelRule("dwell quota needs this field"),
            DwellQuotaMarginRangeRule(8.1, 3555.2),
            DwellQuotaLabelLengthRule(3600),
        ),
    )
}
