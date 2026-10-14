package com.acme.transport.customs.declaration.agreement

/** A rule a capped declaration agreement must pass. Check returns its problems, or none. */
interface CappedDeclarationAgreementRule {
    fun check(item: CappedDeclarationAgreement): MutableList<String>
}

data class CappedDeclarationAgreementRequiredNotesRule(var message: String) : CappedDeclarationAgreementRule {
    override fun check(item: CappedDeclarationAgreement): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDeclarationAgreementRouteCodeLengthRule(var maxLength: Int) : CappedDeclarationAgreementRule {
    override fun check(item: CappedDeclarationAgreement): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedDeclarationAgreementCodeLengthRule(var maxLength: Int) : CappedDeclarationAgreementRule {
    override fun check(item: CappedDeclarationAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedDeclarationAgreementNotesLengthRule(var maxLength: Int) : CappedDeclarationAgreementRule {
    override fun check(item: CappedDeclarationAgreement): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped declaration agreement. */
data class CappedDeclarationAgreementValidator(var rules: MutableList<CappedDeclarationAgreementRule>) {
    fun validate(item: CappedDeclarationAgreement): MutableList<String> {
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

    fun requireValid(item: CappedDeclarationAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedDeclarationAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedDeclarationAgreementValidator(): CappedDeclarationAgreementValidator {
    return CappedDeclarationAgreementValidator(
        mutableListOf<CappedDeclarationAgreementRule>(
            CappedDeclarationAgreementRequiredNotesRule("capped declaration agreement needs an entry"),
            CappedDeclarationAgreementRouteCodeLengthRule(120),
            CappedDeclarationAgreementCodeLengthRule(86400),
            CappedDeclarationAgreementNotesLengthRule(16),
        ),
    )
}
