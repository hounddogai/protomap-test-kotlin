package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

/** A rule a primary fuel surcharge bundle must pass. Check returns its problems, or none. */
interface PrimaryFuelSurchargeBundleRule {
    fun check(item: PrimaryFuelSurchargeBundle): MutableList<String>
}

data class PrimaryFuelSurchargeBundleBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryFuelSurchargeBundleRule {
    override fun check(item: PrimaryFuelSurchargeBundle): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelSurchargeBundleChannelLengthRule(var maxLength: Int) : PrimaryFuelSurchargeBundleRule {
    override fun check(item: PrimaryFuelSurchargeBundle): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelSurchargeBundleCodePrefixRule(var prefix: String) : PrimaryFuelSurchargeBundleRule {
    override fun check(item: PrimaryFuelSurchargeBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelSurchargeBundleScoreRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryFuelSurchargeBundleRule {
    override fun check(item: PrimaryFuelSurchargeBundle): MutableList<String> {
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

data class PrimaryFuelSurchargeBundleCodeLengthRule(var maxLength: Int) : PrimaryFuelSurchargeBundleRule {
    override fun check(item: PrimaryFuelSurchargeBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelSurchargeBundleRequiredLabelRule(var message: String) : PrimaryFuelSurchargeBundleRule {
    override fun check(item: PrimaryFuelSurchargeBundle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary fuel surcharge bundle. */
data class PrimaryFuelSurchargeBundleValidator(var rules: MutableList<PrimaryFuelSurchargeBundleRule>) {
    fun validate(item: PrimaryFuelSurchargeBundle): MutableList<String> {
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

    fun requireValid(item: PrimaryFuelSurchargeBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryFuelSurchargeBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryFuelSurchargeBundleValidator(): PrimaryFuelSurchargeBundleValidator {
    return PrimaryFuelSurchargeBundleValidator(
        mutableListOf<PrimaryFuelSurchargeBundleRule>(
            PrimaryFuelSurchargeBundleBatchSizeRangeRule(16, 64),
            PrimaryFuelSurchargeBundleChannelLengthRule(3600),
            PrimaryFuelSurchargeBundleCodePrefixRule("REF"),
            PrimaryFuelSurchargeBundleScoreRangeRule(5.4, 2011.8),
            PrimaryFuelSurchargeBundleCodeLengthRule(3600),
            PrimaryFuelSurchargeBundleRequiredLabelRule("primary fuel surcharge bundle needs an entry"),
        ),
    )
}
