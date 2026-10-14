package com.acme.transport.freightpay.fuelsurcharge.preference

/** A rule a scheduled fuel surcharge bundle must pass. Check returns its problems, or none. */
interface ScheduledFuelSurchargeBundleRule {
    fun check(item: ScheduledFuelSurchargeBundle): MutableList<String>
}

data class ScheduledFuelSurchargeBundleRequiredNameRule(
    var message: String,
) : ScheduledFuelSurchargeBundleRule {
    override fun check(item: ScheduledFuelSurchargeBundle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelSurchargeBundleRequiredDepotRule(
    var message: String,
) : ScheduledFuelSurchargeBundleRule {
    override fun check(item: ScheduledFuelSurchargeBundle): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelSurchargeBundleRequiredCodeRule(
    var message: String,
) : ScheduledFuelSurchargeBundleRule {
    override fun check(item: ScheduledFuelSurchargeBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelSurchargeBundleCodePrefixRule(var prefix: String) : ScheduledFuelSurchargeBundleRule {
    override fun check(item: ScheduledFuelSurchargeBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled fuel surcharge bundle. */
data class ScheduledFuelSurchargeBundleValidator(var rules: MutableList<ScheduledFuelSurchargeBundleRule>) {
    fun validate(item: ScheduledFuelSurchargeBundle): MutableList<String> {
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

    fun requireValid(item: ScheduledFuelSurchargeBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledFuelSurchargeBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledFuelSurchargeBundleValidator(): ScheduledFuelSurchargeBundleValidator {
    return ScheduledFuelSurchargeBundleValidator(
        mutableListOf<ScheduledFuelSurchargeBundleRule>(
            ScheduledFuelSurchargeBundleRequiredNameRule("scheduled fuel surcharge bundle needs a value"),
            ScheduledFuelSurchargeBundleRequiredDepotRule("scheduled fuel surcharge bundle needs an entry"),
            ScheduledFuelSurchargeBundleRequiredCodeRule("scheduled fuel surcharge bundle needs a value"),
            ScheduledFuelSurchargeBundleCodePrefixRule("X"),
        ),
    )
}
