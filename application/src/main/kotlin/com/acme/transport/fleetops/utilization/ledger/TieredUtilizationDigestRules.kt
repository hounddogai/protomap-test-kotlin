package com.acme.transport.fleetops.utilization.ledger

/** A rule a tiered utilization digest must pass. Check returns its problems, or none. */
interface TieredUtilizationDigestRule {
    fun check(item: TieredUtilizationDigest): MutableList<String>
}

data class TieredUtilizationDigestExternalRefLengthRule(var maxLength: Int) : TieredUtilizationDigestRule {
    override fun check(item: TieredUtilizationDigest): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredUtilizationDigestMaximumRangeRule(
    var min: Double,
    var max: Double,
) : TieredUtilizationDigestRule {
    override fun check(item: TieredUtilizationDigest): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredUtilizationDigestNameLengthRule(var maxLength: Int) : TieredUtilizationDigestRule {
    override fun check(item: TieredUtilizationDigest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredUtilizationDigestCodeLengthRule(var maxLength: Int) : TieredUtilizationDigestRule {
    override fun check(item: TieredUtilizationDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered utilization digest. */
data class TieredUtilizationDigestValidator(var rules: MutableList<TieredUtilizationDigestRule>) {
    fun validate(item: TieredUtilizationDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredUtilizationDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredUtilizationDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredUtilizationDigestValidator(): TieredUtilizationDigestValidator {
    return TieredUtilizationDigestValidator(
        mutableListOf<TieredUtilizationDigestRule>(
            TieredUtilizationDigestExternalRefLengthRule(32),
            TieredUtilizationDigestMaximumRangeRule(4.3, 1916.5),
            TieredUtilizationDigestNameLengthRule(3600),
            TieredUtilizationDigestCodeLengthRule(16),
        ),
    )
}
