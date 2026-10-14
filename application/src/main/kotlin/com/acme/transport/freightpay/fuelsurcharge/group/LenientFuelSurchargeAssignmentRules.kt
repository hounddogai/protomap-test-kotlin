package com.acme.transport.freightpay.fuelsurcharge.group

/** A rule a lenient fuel surcharge assignment must pass. Check returns its problems, or none. */
interface LenientFuelSurchargeAssignmentRule {
    fun check(item: LenientFuelSurchargeAssignment): MutableList<String>
}

data class LenientFuelSurchargeAssignmentLabelLengthRule(
    var maxLength: Int,
) : LenientFuelSurchargeAssignmentRule {
    override fun check(item: LenientFuelSurchargeAssignment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelSurchargeAssignmentCodeLengthRule(
    var maxLength: Int,
) : LenientFuelSurchargeAssignmentRule {
    override fun check(item: LenientFuelSurchargeAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelSurchargeAssignmentRequiredCodeRule(
    var message: String,
) : LenientFuelSurchargeAssignmentRule {
    override fun check(item: LenientFuelSurchargeAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelSurchargeAssignmentCodePrefixRule(
    var prefix: String,
) : LenientFuelSurchargeAssignmentRule {
    override fun check(item: LenientFuelSurchargeAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelSurchargeAssignmentRequiredShiftCodeRule(
    var message: String,
) : LenientFuelSurchargeAssignmentRule {
    override fun check(item: LenientFuelSurchargeAssignment): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient fuel surcharge assignment. */
data class LenientFuelSurchargeAssignmentValidator(var rules: MutableList<LenientFuelSurchargeAssignmentRule>) {
    fun validate(item: LenientFuelSurchargeAssignment): MutableList<String> {
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

    fun requireValid(item: LenientFuelSurchargeAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientFuelSurchargeAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientFuelSurchargeAssignmentValidator(): LenientFuelSurchargeAssignmentValidator {
    return LenientFuelSurchargeAssignmentValidator(
        mutableListOf<LenientFuelSurchargeAssignmentRule>(
            LenientFuelSurchargeAssignmentLabelLengthRule(64),
            LenientFuelSurchargeAssignmentCodeLengthRule(3600),
            LenientFuelSurchargeAssignmentRequiredCodeRule("lenient fuel surcharge assignment needs a value"),
            LenientFuelSurchargeAssignmentCodePrefixRule("Q"),
            LenientFuelSurchargeAssignmentRequiredShiftCodeRule(
                "lenient fuel surcharge assignment needs this field",
            ),
        ),
    )
}
