package com.acme.transport.emissions.evcharge.profile

/** A rule a pending ev charge target must pass. Check returns its problems, or none. */
interface PendingEvChargeTargetRule {
    fun check(item: PendingEvChargeTarget): MutableList<String>
}

data class PendingEvChargeTargetRequiredLabelRule(var message: String) : PendingEvChargeTargetRule {
    override fun check(item: PendingEvChargeTarget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingEvChargeTargetCategoryLengthRule(var maxLength: Int) : PendingEvChargeTargetRule {
    override fun check(item: PendingEvChargeTarget): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingEvChargeTargetRequiredCodeRule(var message: String) : PendingEvChargeTargetRule {
    override fun check(item: PendingEvChargeTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingEvChargeTargetEtaMinutesRangeRule(var min: Int, var max: Int) : PendingEvChargeTargetRule {
    override fun check(item: PendingEvChargeTarget): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a pending ev charge target. */
data class PendingEvChargeTargetValidator(var rules: MutableList<PendingEvChargeTargetRule>) {
    fun validate(item: PendingEvChargeTarget): MutableList<String> {
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

    fun requireValid(item: PendingEvChargeTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingEvChargeTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingEvChargeTargetValidator(): PendingEvChargeTargetValidator {
    return PendingEvChargeTargetValidator(
        mutableListOf<PendingEvChargeTargetRule>(
            PendingEvChargeTargetRequiredLabelRule("pending ev charge target needs an entry"),
            PendingEvChargeTargetCategoryLengthRule(255),
            PendingEvChargeTargetRequiredCodeRule("pending ev charge target needs an entry"),
            PendingEvChargeTargetEtaMinutesRangeRule(120, 255),
        ),
    )
}
