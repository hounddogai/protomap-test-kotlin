package com.acme.transport.freightpay.fuelsurcharge.policy

/** A rule a fuel surcharge policy must pass. Check returns its problems, or none. */
interface FuelSurchargePolicyRule {
    fun check(item: FuelSurchargePolicy): MutableList<String>
}

data class FuelSurchargePolicyRequiredNotesRule(var message: String) : FuelSurchargePolicyRule {
    override fun check(item: FuelSurchargePolicy): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargePolicyNotesLengthRule(var maxLength: Int) : FuelSurchargePolicyRule {
    override fun check(item: FuelSurchargePolicy): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargePolicyTitleLengthRule(var maxLength: Int) : FuelSurchargePolicyRule {
    override fun check(item: FuelSurchargePolicy): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargePolicyRequiredSourceRule(var message: String) : FuelSurchargePolicyRule {
    override fun check(item: FuelSurchargePolicy): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargePolicyArchivedGuardRule(var limit: Int) : FuelSurchargePolicyRule {
    override fun check(item: FuelSurchargePolicy): MutableList<String> {
        if (item.archived && item.stopCount > limit) {
            return mutableListOf<String>("stop count ${item.stopCount} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge policy. */
data class FuelSurchargePolicyValidator(var rules: MutableList<FuelSurchargePolicyRule>) {
    fun validate(item: FuelSurchargePolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelSurchargePolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargePolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargePolicyValidator(): FuelSurchargePolicyValidator {
    return FuelSurchargePolicyValidator(
        mutableListOf<FuelSurchargePolicyRule>(
            FuelSurchargePolicyRequiredNotesRule("fuel surcharge policy needs a value"),
            FuelSurchargePolicyNotesLengthRule(120),
            FuelSurchargePolicyTitleLengthRule(86400),
            FuelSurchargePolicyRequiredSourceRule("fuel surcharge policy needs this field"),
            FuelSurchargePolicyArchivedGuardRule(255),
        ),
    )
}
