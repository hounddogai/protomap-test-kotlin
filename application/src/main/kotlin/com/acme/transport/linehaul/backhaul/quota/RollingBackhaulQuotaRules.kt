package com.acme.transport.linehaul.backhaul.quota

/** A rule a rolling backhaul quota must pass. Check returns its problems, or none. */
interface RollingBackhaulQuotaRule {
    fun check(item: RollingBackhaulQuota): MutableList<String>
}

data class RollingBackhaulQuotaLocaleLengthRule(var maxLength: Int) : RollingBackhaulQuotaRule {
    override fun check(item: RollingBackhaulQuota): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingBackhaulQuotaScoreRangeRule(var min: Double, var max: Double) : RollingBackhaulQuotaRule {
    override fun check(item: RollingBackhaulQuota): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class RollingBackhaulQuotaCodeLengthRule(var maxLength: Int) : RollingBackhaulQuotaRule {
    override fun check(item: RollingBackhaulQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingBackhaulQuotaLabelLengthRule(var maxLength: Int) : RollingBackhaulQuotaRule {
    override fun check(item: RollingBackhaulQuota): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingBackhaulQuotaRequiredCodeRule(var message: String) : RollingBackhaulQuotaRule {
    override fun check(item: RollingBackhaulQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling backhaul quota. */
data class RollingBackhaulQuotaValidator(var rules: MutableList<RollingBackhaulQuotaRule>) {
    fun validate(item: RollingBackhaulQuota): MutableList<String> {
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

    fun requireValid(item: RollingBackhaulQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingBackhaulQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingBackhaulQuotaValidator(): RollingBackhaulQuotaValidator {
    return RollingBackhaulQuotaValidator(
        mutableListOf<RollingBackhaulQuotaRule>(
            RollingBackhaulQuotaLocaleLengthRule(32),
            RollingBackhaulQuotaScoreRangeRule(10.0, 3935.4),
            RollingBackhaulQuotaCodeLengthRule(255),
            RollingBackhaulQuotaLabelLengthRule(255),
            RollingBackhaulQuotaRequiredCodeRule("rolling backhaul quota needs this field"),
        ),
    )
}
