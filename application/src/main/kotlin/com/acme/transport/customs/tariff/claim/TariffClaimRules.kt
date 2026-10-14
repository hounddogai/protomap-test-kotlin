package com.acme.transport.customs.tariff.claim

/** A rule a tariff claim must pass. Check returns its problems, or none. */
interface TariffClaimRule {
    fun check(item: TariffClaim): MutableList<String>
}

data class TariffClaimCodePrefixRule(var prefix: String) : TariffClaimRule {
    override fun check(item: TariffClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TariffClaimRequiredLabelRule(var message: String) : TariffClaimRule {
    override fun check(item: TariffClaim): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffClaimRequiredNotesRule(var message: String) : TariffClaimRule {
    override fun check(item: TariffClaim): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffClaimLoadKgRangeRule(var min: Double, var max: Double) : TariffClaimRule {
    override fun check(item: TariffClaim): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TariffClaimOwnerIdLengthRule(var maxLength: Int) : TariffClaimRule {
    override fun check(item: TariffClaim): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff claim. */
data class TariffClaimValidator(var rules: MutableList<TariffClaimRule>) {
    fun validate(item: TariffClaim): MutableList<String> {
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

    fun requireValid(item: TariffClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffClaimValidator(): TariffClaimValidator {
    return TariffClaimValidator(
        mutableListOf<TariffClaimRule>(
            TariffClaimCodePrefixRule("ACM"),
            TariffClaimRequiredLabelRule("tariff claim needs a value"),
            TariffClaimRequiredNotesRule("tariff claim needs an entry"),
            TariffClaimLoadKgRangeRule(2.2, 4966.9),
            TariffClaimOwnerIdLengthRule(120),
        ),
    )
}
