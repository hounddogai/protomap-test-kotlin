package com.acme.transport.emissions.fuelburn.review

/** A rule a global fuel burn digest must pass. Check returns its problems, or none. */
interface GlobalFuelBurnDigestRule {
    fun check(item: GlobalFuelBurnDigest): MutableList<String>
}

data class GlobalFuelBurnDigestRequiredLabelRule(var message: String) : GlobalFuelBurnDigestRule {
    override fun check(item: GlobalFuelBurnDigest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelBurnDigestRequiredDescriptionRule(var message: String) : GlobalFuelBurnDigestRule {
    override fun check(item: GlobalFuelBurnDigest): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelBurnDigestCodeLengthRule(var maxLength: Int) : GlobalFuelBurnDigestRule {
    override fun check(item: GlobalFuelBurnDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelBurnDigestRequiredCodeRule(var message: String) : GlobalFuelBurnDigestRule {
    override fun check(item: GlobalFuelBurnDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global fuel burn digest. */
data class GlobalFuelBurnDigestValidator(var rules: MutableList<GlobalFuelBurnDigestRule>) {
    fun validate(item: GlobalFuelBurnDigest): MutableList<String> {
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

    fun requireValid(item: GlobalFuelBurnDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalFuelBurnDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalFuelBurnDigestValidator(): GlobalFuelBurnDigestValidator {
    return GlobalFuelBurnDigestValidator(
        mutableListOf<GlobalFuelBurnDigestRule>(
            GlobalFuelBurnDigestRequiredLabelRule("global fuel burn digest needs this field"),
            GlobalFuelBurnDigestRequiredDescriptionRule("global fuel burn digest needs this field"),
            GlobalFuelBurnDigestCodeLengthRule(16),
            GlobalFuelBurnDigestRequiredCodeRule("global fuel burn digest needs a value"),
        ),
    )
}
