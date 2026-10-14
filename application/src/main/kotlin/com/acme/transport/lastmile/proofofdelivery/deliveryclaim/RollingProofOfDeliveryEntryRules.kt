package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

/** A rule a rolling proof of delivery entry must pass. Check returns its problems, or none. */
interface RollingProofOfDeliveryEntryRule {
    fun check(item: RollingProofOfDeliveryEntry): MutableList<String>
}

data class RollingProofOfDeliveryEntryCodePrefixRule(var prefix: String) : RollingProofOfDeliveryEntryRule {
    override fun check(item: RollingProofOfDeliveryEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingProofOfDeliveryEntryCodeLengthRule(var maxLength: Int) : RollingProofOfDeliveryEntryRule {
    override fun check(item: RollingProofOfDeliveryEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingProofOfDeliveryEntryPriorityRangeRule(
    var min: Int,
    var max: Int,
) : RollingProofOfDeliveryEntryRule {
    override fun check(item: RollingProofOfDeliveryEntry): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class RollingProofOfDeliveryEntryRequiredCodeRule(var message: String) : RollingProofOfDeliveryEntryRule {
    override fun check(item: RollingProofOfDeliveryEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingProofOfDeliveryEntryPalletsRangeRule(
    var min: Int,
    var max: Int,
) : RollingProofOfDeliveryEntryRule {
    override fun check(item: RollingProofOfDeliveryEntry): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling proof of delivery entry. */
data class RollingProofOfDeliveryEntryValidator(var rules: MutableList<RollingProofOfDeliveryEntryRule>) {
    fun validate(item: RollingProofOfDeliveryEntry): MutableList<String> {
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

    fun requireValid(item: RollingProofOfDeliveryEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingProofOfDeliveryEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingProofOfDeliveryEntryValidator(): RollingProofOfDeliveryEntryValidator {
    return RollingProofOfDeliveryEntryValidator(
        mutableListOf<RollingProofOfDeliveryEntryRule>(
            RollingProofOfDeliveryEntryCodePrefixRule("ACM"),
            RollingProofOfDeliveryEntryCodeLengthRule(255),
            RollingProofOfDeliveryEntryPriorityRangeRule(32, 64),
            RollingProofOfDeliveryEntryRequiredCodeRule("rolling proof of delivery entry needs an entry"),
            RollingProofOfDeliveryEntryPalletsRangeRule(16, 120),
        ),
    )
}
