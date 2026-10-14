package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

/** A rule a capped backhaul template must pass. Check returns its problems, or none. */
interface CappedBackhaulTemplateRule {
    fun check(item: CappedBackhaulTemplate): MutableList<String>
}

data class CappedBackhaulTemplateScoreRangeRule(var min: Double, var max: Double) : CappedBackhaulTemplateRule {
    override fun check(item: CappedBackhaulTemplate): MutableList<String> {
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

data class CappedBackhaulTemplateCodeLengthRule(var maxLength: Int) : CappedBackhaulTemplateRule {
    override fun check(item: CappedBackhaulTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedBackhaulTemplateVehicleIdLengthRule(var maxLength: Int) : CappedBackhaulTemplateRule {
    override fun check(item: CappedBackhaulTemplate): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedBackhaulTemplateExpiresAtRangeRule(var min: Int, var max: Int) : CappedBackhaulTemplateRule {
    override fun check(item: CappedBackhaulTemplate): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class CappedBackhaulTemplatePinnedGuardRule(var limit: Double) : CappedBackhaulTemplateRule {
    override fun check(item: CappedBackhaulTemplate): MutableList<String> {
        if (item.pinned && item.fuelLiters > limit) {
            return mutableListOf<String>("fuel liters ${item.fuelLiters} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped backhaul template. */
data class CappedBackhaulTemplateValidator(var rules: MutableList<CappedBackhaulTemplateRule>) {
    fun validate(item: CappedBackhaulTemplate): MutableList<String> {
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

    fun requireValid(item: CappedBackhaulTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedBackhaulTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedBackhaulTemplateValidator(): CappedBackhaulTemplateValidator {
    return CappedBackhaulTemplateValidator(
        mutableListOf<CappedBackhaulTemplateRule>(
            CappedBackhaulTemplateScoreRangeRule(7.7, 1217.1),
            CappedBackhaulTemplateCodeLengthRule(255),
            CappedBackhaulTemplateVehicleIdLengthRule(3600),
            CappedBackhaulTemplateExpiresAtRangeRule(80, 120),
            CappedBackhaulTemplatePinnedGuardRule(0.4),
        ),
    )
}
