package com.acme.transport.freightpay.fuelsurcharge.assignment

/** A rule a primary fuel surcharge assignment must pass. Check returns its problems, or none. */
interface PrimaryFuelSurchargeAssignmentRule {
    fun check(item: PrimaryFuelSurchargeAssignment): MutableList<String>
}

data class PrimaryFuelSurchargeAssignmentCarrierLengthRule(
    var maxLength: Int,
) : PrimaryFuelSurchargeAssignmentRule {
    override fun check(item: PrimaryFuelSurchargeAssignment): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelSurchargeAssignmentCodePrefixRule(
    var prefix: String,
) : PrimaryFuelSurchargeAssignmentRule {
    override fun check(item: PrimaryFuelSurchargeAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelSurchargeAssignmentRequiredLabelRule(
    var message: String,
) : PrimaryFuelSurchargeAssignmentRule {
    override fun check(item: PrimaryFuelSurchargeAssignment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelSurchargeAssignmentVisibleGuardRule(
    var limit: Double,
) : PrimaryFuelSurchargeAssignmentRule {
    override fun check(item: PrimaryFuelSurchargeAssignment): MutableList<String> {
        if (item.visible && item.fuelLiters > limit) {
            return mutableListOf<String>("fuel liters ${item.fuelLiters} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelSurchargeAssignmentTitleLengthRule(
    var maxLength: Int,
) : PrimaryFuelSurchargeAssignmentRule {
    override fun check(item: PrimaryFuelSurchargeAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelSurchargeAssignmentRequiredCarrierRule(
    var message: String,
) : PrimaryFuelSurchargeAssignmentRule {
    override fun check(item: PrimaryFuelSurchargeAssignment): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary fuel surcharge assignment. */
data class PrimaryFuelSurchargeAssignmentValidator(var rules: MutableList<PrimaryFuelSurchargeAssignmentRule>) {
    fun validate(item: PrimaryFuelSurchargeAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryFuelSurchargeAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryFuelSurchargeAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryFuelSurchargeAssignmentValidator(): PrimaryFuelSurchargeAssignmentValidator {
    return PrimaryFuelSurchargeAssignmentValidator(
        mutableListOf<PrimaryFuelSurchargeAssignmentRule>(
            PrimaryFuelSurchargeAssignmentCarrierLengthRule(255),
            PrimaryFuelSurchargeAssignmentCodePrefixRule("Q"),
            PrimaryFuelSurchargeAssignmentRequiredLabelRule(
                "primary fuel surcharge assignment needs this field",
            ),
            PrimaryFuelSurchargeAssignmentVisibleGuardRule(7.9),
            PrimaryFuelSurchargeAssignmentTitleLengthRule(3600),
            PrimaryFuelSurchargeAssignmentRequiredCarrierRule("primary fuel surcharge assignment needs a value"),
        ),
    )
}
