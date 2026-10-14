package com.acme.transport.freightpay.audit.mapping

/** A rule a tiered audit manifest must pass. Check returns its problems, or none. */
interface TieredAuditManifestRule {
    fun check(item: TieredAuditManifest): MutableList<String>
}

data class TieredAuditManifestFloorValueRangeRule(var min: Double, var max: Double) : TieredAuditManifestRule {
    override fun check(item: TieredAuditManifest): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredAuditManifestTitleLengthRule(var maxLength: Int) : TieredAuditManifestRule {
    override fun check(item: TieredAuditManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredAuditManifestCodePrefixRule(var prefix: String) : TieredAuditManifestRule {
    override fun check(item: TieredAuditManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredAuditManifestThresholdRangeRule(var min: Double, var max: Double) : TieredAuditManifestRule {
    override fun check(item: TieredAuditManifest): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class TieredAuditManifestCreatedAtRangeRule(var min: Int, var max: Int) : TieredAuditManifestRule {
    override fun check(item: TieredAuditManifest): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredAuditManifestWeightRangeRule(var min: Double, var max: Double) : TieredAuditManifestRule {
    override fun check(item: TieredAuditManifest): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered audit manifest. */
data class TieredAuditManifestValidator(var rules: MutableList<TieredAuditManifestRule>) {
    fun validate(item: TieredAuditManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredAuditManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredAuditManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredAuditManifestValidator(): TieredAuditManifestValidator {
    return TieredAuditManifestValidator(
        mutableListOf<TieredAuditManifestRule>(
            TieredAuditManifestFloorValueRangeRule(9.2, 1806.2),
            TieredAuditManifestTitleLengthRule(16),
            TieredAuditManifestCodePrefixRule("OPS"),
            TieredAuditManifestThresholdRangeRule(1.9, 1787.1),
            TieredAuditManifestCreatedAtRangeRule(32, 80),
            TieredAuditManifestWeightRangeRule(1.8, 3796.3),
        ),
    )
}
