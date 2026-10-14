package com.acme.transport.crewing.swap.adjustment

/** A rule a legacy swap agreement must pass. Check returns its problems, or none. */
interface LegacySwapAgreementRule {
    fun check(item: LegacySwapAgreement): MutableList<String>
}

data class LegacySwapAgreementRequiredChecksumRule(var message: String) : LegacySwapAgreementRule {
    override fun check(item: LegacySwapAgreement): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacySwapAgreementRequiredCodeRule(var message: String) : LegacySwapAgreementRule {
    override fun check(item: LegacySwapAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacySwapAgreementLabelLengthRule(var maxLength: Int) : LegacySwapAgreementRule {
    override fun check(item: LegacySwapAgreement): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacySwapAgreementCodePrefixRule(var prefix: String) : LegacySwapAgreementRule {
    override fun check(item: LegacySwapAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy swap agreement. */
data class LegacySwapAgreementValidator(var rules: MutableList<LegacySwapAgreementRule>) {
    fun validate(item: LegacySwapAgreement): MutableList<String> {
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

    fun requireValid(item: LegacySwapAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacySwapAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacySwapAgreementValidator(): LegacySwapAgreementValidator {
    return LegacySwapAgreementValidator(
        mutableListOf<LegacySwapAgreementRule>(
            LegacySwapAgreementRequiredChecksumRule("legacy swap agreement needs this field"),
            LegacySwapAgreementRequiredCodeRule("legacy swap agreement needs a value"),
            LegacySwapAgreementLabelLengthRule(16),
            LegacySwapAgreementCodePrefixRule("ACM"),
        ),
    )
}
