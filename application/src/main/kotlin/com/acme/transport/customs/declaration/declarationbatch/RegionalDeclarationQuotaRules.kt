package com.acme.transport.customs.declaration.declarationbatch

/** A rule a regional declaration quota must pass. Check returns its problems, or none. */
interface RegionalDeclarationQuotaRule {
    fun check(item: RegionalDeclarationQuota): MutableList<String>
}

data class RegionalDeclarationQuotaRequiredSegmentRule(var message: String) : RegionalDeclarationQuotaRule {
    override fun check(item: RegionalDeclarationQuota): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDeclarationQuotaCodePrefixRule(var prefix: String) : RegionalDeclarationQuotaRule {
    override fun check(item: RegionalDeclarationQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDeclarationQuotaRequiredLabelRule(var message: String) : RegionalDeclarationQuotaRule {
    override fun check(item: RegionalDeclarationQuota): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDeclarationQuotaLabelLengthRule(var maxLength: Int) : RegionalDeclarationQuotaRule {
    override fun check(item: RegionalDeclarationQuota): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDeclarationQuotaRequiredNameRule(var message: String) : RegionalDeclarationQuotaRule {
    override fun check(item: RegionalDeclarationQuota): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional declaration quota. */
data class RegionalDeclarationQuotaValidator(var rules: MutableList<RegionalDeclarationQuotaRule>) {
    fun validate(item: RegionalDeclarationQuota): MutableList<String> {
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

    fun requireValid(item: RegionalDeclarationQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDeclarationQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDeclarationQuotaValidator(): RegionalDeclarationQuotaValidator {
    return RegionalDeclarationQuotaValidator(
        mutableListOf<RegionalDeclarationQuotaRule>(
            RegionalDeclarationQuotaRequiredSegmentRule("regional declaration quota needs a value"),
            RegionalDeclarationQuotaCodePrefixRule("OPS"),
            RegionalDeclarationQuotaRequiredLabelRule("regional declaration quota needs this field"),
            RegionalDeclarationQuotaLabelLengthRule(32),
            RegionalDeclarationQuotaRequiredNameRule("regional declaration quota needs an entry"),
        ),
    )
}
