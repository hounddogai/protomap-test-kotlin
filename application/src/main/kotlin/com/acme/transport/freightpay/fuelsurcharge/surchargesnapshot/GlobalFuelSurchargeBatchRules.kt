package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

/** A rule a global fuel surcharge batch must pass. Check returns its problems, or none. */
interface GlobalFuelSurchargeBatchRule {
    fun check(item: GlobalFuelSurchargeBatch): MutableList<String>
}

data class GlobalFuelSurchargeBatchRetriesRangeRule(var min: Int, var max: Int) : GlobalFuelSurchargeBatchRule {
    override fun check(item: GlobalFuelSurchargeBatch): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelSurchargeBatchNameLengthRule(var maxLength: Int) : GlobalFuelSurchargeBatchRule {
    override fun check(item: GlobalFuelSurchargeBatch): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelSurchargeBatchRequiredCodeRule(var message: String) : GlobalFuelSurchargeBatchRule {
    override fun check(item: GlobalFuelSurchargeBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelSurchargeBatchUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : GlobalFuelSurchargeBatchRule {
    override fun check(item: GlobalFuelSurchargeBatch): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global fuel surcharge batch. */
data class GlobalFuelSurchargeBatchValidator(var rules: MutableList<GlobalFuelSurchargeBatchRule>) {
    fun validate(item: GlobalFuelSurchargeBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalFuelSurchargeBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalFuelSurchargeBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalFuelSurchargeBatchValidator(): GlobalFuelSurchargeBatchValidator {
    return GlobalFuelSurchargeBatchValidator(
        mutableListOf<GlobalFuelSurchargeBatchRule>(
            GlobalFuelSurchargeBatchRetriesRangeRule(255, 3600),
            GlobalFuelSurchargeBatchNameLengthRule(255),
            GlobalFuelSurchargeBatchRequiredCodeRule("global fuel surcharge batch needs this field"),
            GlobalFuelSurchargeBatchUpdatedAtRangeRule(80, 255),
        ),
    )
}
