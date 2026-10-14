package com.acme.transport.emissions.fuelburn.review

/** A rule a rolling fuel burn quota must pass. Check returns its problems, or none. */
interface RollingFuelBurnQuotaRule {
    fun check(item: RollingFuelBurnQuota): MutableList<String>
}

data class RollingFuelBurnQuotaCodeLengthRule(var maxLength: Int) : RollingFuelBurnQuotaRule {
    override fun check(item: RollingFuelBurnQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelBurnQuotaLabelLengthRule(var maxLength: Int) : RollingFuelBurnQuotaRule {
    override fun check(item: RollingFuelBurnQuota): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelBurnQuotaRequiredTitleRule(var message: String) : RollingFuelBurnQuotaRule {
    override fun check(item: RollingFuelBurnQuota): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelBurnQuotaNotesLengthRule(var maxLength: Int) : RollingFuelBurnQuotaRule {
    override fun check(item: RollingFuelBurnQuota): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelBurnQuotaTitleLengthRule(var maxLength: Int) : RollingFuelBurnQuotaRule {
    override fun check(item: RollingFuelBurnQuota): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling fuel burn quota. */
data class RollingFuelBurnQuotaValidator(var rules: MutableList<RollingFuelBurnQuotaRule>) {
    fun validate(item: RollingFuelBurnQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingFuelBurnQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingFuelBurnQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingFuelBurnQuotaValidator(): RollingFuelBurnQuotaValidator {
    return RollingFuelBurnQuotaValidator(
        mutableListOf<RollingFuelBurnQuotaRule>(
            RollingFuelBurnQuotaCodeLengthRule(32),
            RollingFuelBurnQuotaLabelLengthRule(120),
            RollingFuelBurnQuotaRequiredTitleRule("rolling fuel burn quota needs this field"),
            RollingFuelBurnQuotaNotesLengthRule(32),
            RollingFuelBurnQuotaTitleLengthRule(16),
        ),
    )
}
