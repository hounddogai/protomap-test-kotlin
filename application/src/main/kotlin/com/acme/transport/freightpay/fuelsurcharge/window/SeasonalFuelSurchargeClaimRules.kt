package com.acme.transport.freightpay.fuelsurcharge.window

/** A rule a seasonal fuel surcharge claim must pass. Check returns its problems, or none. */
interface SeasonalFuelSurchargeClaimRule {
    fun check(item: SeasonalFuelSurchargeClaim): MutableList<String>
}

data class SeasonalFuelSurchargeClaimOwnerIdLengthRule(var maxLength: Int) : SeasonalFuelSurchargeClaimRule {
    override fun check(item: SeasonalFuelSurchargeClaim): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFuelSurchargeClaimChecksumLengthRule(var maxLength: Int) : SeasonalFuelSurchargeClaimRule {
    override fun check(item: SeasonalFuelSurchargeClaim): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFuelSurchargeClaimRequiredCodeRule(var message: String) : SeasonalFuelSurchargeClaimRule {
    override fun check(item: SeasonalFuelSurchargeClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal fuel surcharge claim. */
data class SeasonalFuelSurchargeClaimValidator(var rules: MutableList<SeasonalFuelSurchargeClaimRule>) {
    fun validate(item: SeasonalFuelSurchargeClaim): MutableList<String> {
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

    fun requireValid(item: SeasonalFuelSurchargeClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalFuelSurchargeClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalFuelSurchargeClaimValidator(): SeasonalFuelSurchargeClaimValidator {
    return SeasonalFuelSurchargeClaimValidator(
        mutableListOf<SeasonalFuelSurchargeClaimRule>(
            SeasonalFuelSurchargeClaimOwnerIdLengthRule(16),
            SeasonalFuelSurchargeClaimChecksumLengthRule(1000),
            SeasonalFuelSurchargeClaimRequiredCodeRule("seasonal fuel surcharge claim needs an entry"),
        ),
    )
}
