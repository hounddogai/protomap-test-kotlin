package com.acme.transport.network.cutoff.preference

/** A rule a cutoff agreement must pass. Check returns its problems, or none. */
interface CutoffAgreementRule {
    fun check(item: CutoffAgreement): MutableList<String>
}

data class CutoffAgreementCodePrefixRule(var prefix: String) : CutoffAgreementRule {
    override fun check(item: CutoffAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CutoffAgreementRatioRangeRule(var min: Double, var max: Double) : CutoffAgreementRule {
    override fun check(item: CutoffAgreement): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class CutoffAgreementRequiredRegionRule(var message: String) : CutoffAgreementRule {
    override fun check(item: CutoffAgreement): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffAgreementCodeLengthRule(var maxLength: Int) : CutoffAgreementRule {
    override fun check(item: CutoffAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffAgreementVersionRangeRule(var min: Int, var max: Int) : CutoffAgreementRule {
    override fun check(item: CutoffAgreement): MutableList<String> {
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

/** Runs every rule of a cutoff agreement. */
data class CutoffAgreementValidator(var rules: MutableList<CutoffAgreementRule>) {
    fun validate(item: CutoffAgreement): MutableList<String> {
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

    fun requireValid(item: CutoffAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffAgreementValidator(): CutoffAgreementValidator {
    return CutoffAgreementValidator(
        mutableListOf<CutoffAgreementRule>(
            CutoffAgreementCodePrefixRule("X"),
            CutoffAgreementRatioRangeRule(7.2, 4591.7),
            CutoffAgreementRequiredRegionRule("cutoff agreement needs an entry"),
            CutoffAgreementCodeLengthRule(86400),
            CutoffAgreementVersionRangeRule(80, 1000),
        ),
    )
}
