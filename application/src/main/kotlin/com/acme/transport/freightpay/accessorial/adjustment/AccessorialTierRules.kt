package com.acme.transport.freightpay.accessorial.adjustment

/** A rule a accessorial tier must pass. Check returns its problems, or none. */
interface AccessorialTierRule {
    fun check(item: AccessorialTier): MutableList<String>
}

data class AccessorialTierLabelLengthRule(var maxLength: Int) : AccessorialTierRule {
    override fun check(item: AccessorialTier): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialTierRequiredLabelRule(var message: String) : AccessorialTierRule {
    override fun check(item: AccessorialTier): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialTierRequiredCarrierRule(var message: String) : AccessorialTierRule {
    override fun check(item: AccessorialTier): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial tier. */
data class AccessorialTierValidator(var rules: MutableList<AccessorialTierRule>) {
    fun validate(item: AccessorialTier): MutableList<String> {
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

    fun requireValid(item: AccessorialTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialTier>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialTierValidator(): AccessorialTierValidator {
    return AccessorialTierValidator(
        mutableListOf<AccessorialTierRule>(
            AccessorialTierLabelLengthRule(1000),
            AccessorialTierRequiredLabelRule("accessorial tier needs a value"),
            AccessorialTierRequiredCarrierRule("accessorial tier needs an entry"),
        ),
    )
}
