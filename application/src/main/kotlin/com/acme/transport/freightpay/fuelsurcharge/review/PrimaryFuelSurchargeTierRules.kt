package com.acme.transport.freightpay.fuelsurcharge.review

/** A rule a primary fuel surcharge tier must pass. Check returns its problems, or none. */
interface PrimaryFuelSurchargeTierRule {
    fun check(item: PrimaryFuelSurchargeTier): MutableList<String>
}

data class PrimaryFuelSurchargeTierLabelLengthRule(var maxLength: Int) : PrimaryFuelSurchargeTierRule {
    override fun check(item: PrimaryFuelSurchargeTier): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelSurchargeTierRetriesRangeRule(var min: Int, var max: Int) : PrimaryFuelSurchargeTierRule {
    override fun check(item: PrimaryFuelSurchargeTier): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class PrimaryFuelSurchargeTierNameLengthRule(var maxLength: Int) : PrimaryFuelSurchargeTierRule {
    override fun check(item: PrimaryFuelSurchargeTier): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelSurchargeTierRequiredCodeRule(var message: String) : PrimaryFuelSurchargeTierRule {
    override fun check(item: PrimaryFuelSurchargeTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary fuel surcharge tier. */
data class PrimaryFuelSurchargeTierValidator(var rules: MutableList<PrimaryFuelSurchargeTierRule>) {
    fun validate(item: PrimaryFuelSurchargeTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryFuelSurchargeTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryFuelSurchargeTier>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryFuelSurchargeTierValidator(): PrimaryFuelSurchargeTierValidator {
    return PrimaryFuelSurchargeTierValidator(
        mutableListOf<PrimaryFuelSurchargeTierRule>(
            PrimaryFuelSurchargeTierLabelLengthRule(3600),
            PrimaryFuelSurchargeTierRetriesRangeRule(32, 255),
            PrimaryFuelSurchargeTierNameLengthRule(80),
            PrimaryFuelSurchargeTierRequiredCodeRule("primary fuel surcharge tier needs a value"),
        ),
    )
}
