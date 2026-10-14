package com.acme.transport.lastmile.attempt.manifest

/** A rule a attempt manifest must pass. Check returns its problems, or none. */
interface AttemptManifestRule {
    fun check(item: AttemptManifest): MutableList<String>
}

data class AttemptManifestRequiredCodeRule(var message: String) : AttemptManifestRule {
    override fun check(item: AttemptManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptManifestShiftCodeLengthRule(var maxLength: Int) : AttemptManifestRule {
    override fun check(item: AttemptManifest): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptManifestRequiredNameRule(var message: String) : AttemptManifestRule {
    override fun check(item: AttemptManifest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt manifest. */
data class AttemptManifestValidator(var rules: MutableList<AttemptManifestRule>) {
    fun validate(item: AttemptManifest): MutableList<String> {
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

    fun requireValid(item: AttemptManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptManifestValidator(): AttemptManifestValidator {
    return AttemptManifestValidator(
        mutableListOf<AttemptManifestRule>(
            AttemptManifestRequiredCodeRule("attempt manifest needs an entry"),
            AttemptManifestShiftCodeLengthRule(1000),
            AttemptManifestRequiredNameRule("attempt manifest needs this field"),
        ),
    )
}
