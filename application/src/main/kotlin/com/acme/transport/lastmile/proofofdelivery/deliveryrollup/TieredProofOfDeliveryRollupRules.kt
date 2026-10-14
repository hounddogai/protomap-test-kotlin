package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

/** A rule a tiered proof of delivery rollup must pass. Check returns its problems, or none. */
interface TieredProofOfDeliveryRollupRule {
    fun check(item: TieredProofOfDeliveryRollup): MutableList<String>
}

data class TieredProofOfDeliveryRollupFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : TieredProofOfDeliveryRollupRule {
    override fun check(item: TieredProofOfDeliveryRollup): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class TieredProofOfDeliveryRollupDriverIdLengthRule(var maxLength: Int) : TieredProofOfDeliveryRollupRule {
    override fun check(item: TieredProofOfDeliveryRollup): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredProofOfDeliveryRollupCodePrefixRule(var prefix: String) : TieredProofOfDeliveryRollupRule {
    override fun check(item: TieredProofOfDeliveryRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered proof of delivery rollup. */
data class TieredProofOfDeliveryRollupValidator(var rules: MutableList<TieredProofOfDeliveryRollupRule>) {
    fun validate(item: TieredProofOfDeliveryRollup): MutableList<String> {
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

    fun requireValid(item: TieredProofOfDeliveryRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredProofOfDeliveryRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredProofOfDeliveryRollupValidator(): TieredProofOfDeliveryRollupValidator {
    return TieredProofOfDeliveryRollupValidator(
        mutableListOf<TieredProofOfDeliveryRollupRule>(
            TieredProofOfDeliveryRollupFloorValueRangeRule(6.6, 4587.5),
            TieredProofOfDeliveryRollupDriverIdLengthRule(86400),
            TieredProofOfDeliveryRollupCodePrefixRule("X"),
        ),
    )
}
