package com.acme.transport.lastmile.proofofdelivery.window

/** A rule a proof of delivery window must pass. Check returns its problems, or none. */
interface ProofOfDeliveryWindowRule {
    fun check(item: ProofOfDeliveryWindow): MutableList<String>
}

data class ProofOfDeliveryWindowRequiredCodeRule(var message: String) : ProofOfDeliveryWindowRule {
    override fun check(item: ProofOfDeliveryWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryWindowNameLengthRule(var maxLength: Int) : ProofOfDeliveryWindowRule {
    override fun check(item: ProofOfDeliveryWindow): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryWindowCodeLengthRule(var maxLength: Int) : ProofOfDeliveryWindowRule {
    override fun check(item: ProofOfDeliveryWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryWindowCodePrefixRule(var prefix: String) : ProofOfDeliveryWindowRule {
    override fun check(item: ProofOfDeliveryWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryWindowCountryLengthRule(var maxLength: Int) : ProofOfDeliveryWindowRule {
    override fun check(item: ProofOfDeliveryWindow): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery window. */
data class ProofOfDeliveryWindowValidator(var rules: MutableList<ProofOfDeliveryWindowRule>) {
    fun validate(item: ProofOfDeliveryWindow): MutableList<String> {
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

    fun requireValid(item: ProofOfDeliveryWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryWindowValidator(): ProofOfDeliveryWindowValidator {
    return ProofOfDeliveryWindowValidator(
        mutableListOf<ProofOfDeliveryWindowRule>(
            ProofOfDeliveryWindowRequiredCodeRule("proof of delivery window needs an entry"),
            ProofOfDeliveryWindowNameLengthRule(86400),
            ProofOfDeliveryWindowCodeLengthRule(80),
            ProofOfDeliveryWindowCodePrefixRule("ACM"),
            ProofOfDeliveryWindowCountryLengthRule(3600),
        ),
    )
}
