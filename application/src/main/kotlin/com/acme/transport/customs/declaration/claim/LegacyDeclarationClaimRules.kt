package com.acme.transport.customs.declaration.claim

/** A rule a legacy declaration claim must pass. Check returns its problems, or none. */
interface LegacyDeclarationClaimRule {
    fun check(item: LegacyDeclarationClaim): MutableList<String>
}

data class LegacyDeclarationClaimRequiredCodeRule(var message: String) : LegacyDeclarationClaimRule {
    override fun check(item: LegacyDeclarationClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDeclarationClaimCodeLengthRule(var maxLength: Int) : LegacyDeclarationClaimRule {
    override fun check(item: LegacyDeclarationClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDeclarationClaimVersionRangeRule(var min: Int, var max: Int) : LegacyDeclarationClaimRule {
    override fun check(item: LegacyDeclarationClaim): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class LegacyDeclarationClaimExpiresAtRangeRule(var min: Int, var max: Int) : LegacyDeclarationClaimRule {
    override fun check(item: LegacyDeclarationClaim): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy declaration claim. */
data class LegacyDeclarationClaimValidator(var rules: MutableList<LegacyDeclarationClaimRule>) {
    fun validate(item: LegacyDeclarationClaim): MutableList<String> {
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

    fun requireValid(item: LegacyDeclarationClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDeclarationClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDeclarationClaimValidator(): LegacyDeclarationClaimValidator {
    return LegacyDeclarationClaimValidator(
        mutableListOf<LegacyDeclarationClaimRule>(
            LegacyDeclarationClaimRequiredCodeRule("legacy declaration claim needs this field"),
            LegacyDeclarationClaimCodeLengthRule(1000),
            LegacyDeclarationClaimVersionRangeRule(64, 1000),
            LegacyDeclarationClaimExpiresAtRangeRule(32, 64),
        ),
    )
}
