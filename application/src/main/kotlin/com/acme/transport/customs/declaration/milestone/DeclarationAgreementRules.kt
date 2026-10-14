package com.acme.transport.customs.declaration.milestone

/** A rule a declaration agreement must pass. Check returns its problems, or none. */
interface DeclarationAgreementRule {
    fun check(item: DeclarationAgreement): MutableList<String>
}

data class DeclarationAgreementVehicleIdLengthRule(var maxLength: Int) : DeclarationAgreementRule {
    override fun check(item: DeclarationAgreement): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationAgreementCodePrefixRule(var prefix: String) : DeclarationAgreementRule {
    override fun check(item: DeclarationAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationAgreementRequiredCodeRule(var message: String) : DeclarationAgreementRule {
    override fun check(item: DeclarationAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationAgreementChannelLengthRule(var maxLength: Int) : DeclarationAgreementRule {
    override fun check(item: DeclarationAgreement): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration agreement. */
data class DeclarationAgreementValidator(var rules: MutableList<DeclarationAgreementRule>) {
    fun validate(item: DeclarationAgreement): MutableList<String> {
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

    fun requireValid(item: DeclarationAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationAgreementValidator(): DeclarationAgreementValidator {
    return DeclarationAgreementValidator(
        mutableListOf<DeclarationAgreementRule>(
            DeclarationAgreementVehicleIdLengthRule(32),
            DeclarationAgreementCodePrefixRule("REF"),
            DeclarationAgreementRequiredCodeRule("declaration agreement needs a value"),
            DeclarationAgreementChannelLengthRule(1000),
        ),
    )
}
