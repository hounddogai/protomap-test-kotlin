package com.acme.transport.network.servicearea.primaryareatarget

/** A rule a manual service area variance must pass. Check returns its problems, or none. */
interface ManualServiceAreaVarianceRule {
    fun check(item: ManualServiceAreaVariance): MutableList<String>
}

data class ManualServiceAreaVarianceCodeLengthRule(var maxLength: Int) : ManualServiceAreaVarianceRule {
    override fun check(item: ManualServiceAreaVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualServiceAreaVarianceAmountRangeRule(
    var min: Double,
    var max: Double,
) : ManualServiceAreaVarianceRule {
    override fun check(item: ManualServiceAreaVariance): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class ManualServiceAreaVarianceRequiredOwnerIdRule(var message: String) : ManualServiceAreaVarianceRule {
    override fun check(item: ManualServiceAreaVariance): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualServiceAreaVarianceDriverIdLengthRule(var maxLength: Int) : ManualServiceAreaVarianceRule {
    override fun check(item: ManualServiceAreaVariance): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualServiceAreaVarianceNameLengthRule(var maxLength: Int) : ManualServiceAreaVarianceRule {
    override fun check(item: ManualServiceAreaVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualServiceAreaVarianceOwnerIdLengthRule(var maxLength: Int) : ManualServiceAreaVarianceRule {
    override fun check(item: ManualServiceAreaVariance): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual service area variance. */
data class ManualServiceAreaVarianceValidator(var rules: MutableList<ManualServiceAreaVarianceRule>) {
    fun validate(item: ManualServiceAreaVariance): MutableList<String> {
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

    fun requireValid(item: ManualServiceAreaVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualServiceAreaVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultManualServiceAreaVarianceValidator(): ManualServiceAreaVarianceValidator {
    return ManualServiceAreaVarianceValidator(
        mutableListOf<ManualServiceAreaVarianceRule>(
            ManualServiceAreaVarianceCodeLengthRule(86400),
            ManualServiceAreaVarianceAmountRangeRule(9.1, 825.8),
            ManualServiceAreaVarianceRequiredOwnerIdRule("manual service area variance needs an entry"),
            ManualServiceAreaVarianceDriverIdLengthRule(86400),
            ManualServiceAreaVarianceNameLengthRule(86400),
            ManualServiceAreaVarianceOwnerIdLengthRule(120),
        ),
    )
}
