package com.acme.transport.terminal.sortation.sortationdigest

/** A rule a pending sortation digest must pass. Check returns its problems, or none. */
interface PendingSortationDigestRule {
    fun check(item: PendingSortationDigest): MutableList<String>
}

data class PendingSortationDigestLabelLengthRule(var maxLength: Int) : PendingSortationDigestRule {
    override fun check(item: PendingSortationDigest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingSortationDigestLocaleLengthRule(var maxLength: Int) : PendingSortationDigestRule {
    override fun check(item: PendingSortationDigest): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingSortationDigestRequiredLabelRule(var message: String) : PendingSortationDigestRule {
    override fun check(item: PendingSortationDigest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingSortationDigestRequiredCodeRule(var message: String) : PendingSortationDigestRule {
    override fun check(item: PendingSortationDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending sortation digest. */
data class PendingSortationDigestValidator(var rules: MutableList<PendingSortationDigestRule>) {
    fun validate(item: PendingSortationDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingSortationDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingSortationDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingSortationDigestValidator(): PendingSortationDigestValidator {
    return PendingSortationDigestValidator(
        mutableListOf<PendingSortationDigestRule>(
            PendingSortationDigestLabelLengthRule(120),
            PendingSortationDigestLocaleLengthRule(80),
            PendingSortationDigestRequiredLabelRule("pending sortation digest needs an entry"),
            PendingSortationDigestRequiredCodeRule("pending sortation digest needs a value"),
        ),
    )
}
