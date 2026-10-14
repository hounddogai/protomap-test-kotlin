package com.acme.transport.customs.declaration.declarationsnapshot

/** A rule a global declaration snapshot must pass. Check returns its problems, or none. */
interface GlobalDeclarationSnapshotRule {
    fun check(item: GlobalDeclarationSnapshot): MutableList<String>
}

data class GlobalDeclarationSnapshotLabelLengthRule(var maxLength: Int) : GlobalDeclarationSnapshotRule {
    override fun check(item: GlobalDeclarationSnapshot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDeclarationSnapshotArchivedGuardRule(var limit: Int) : GlobalDeclarationSnapshotRule {
    override fun check(item: GlobalDeclarationSnapshot): MutableList<String> {
        if (item.archived && item.retries > limit) {
            return mutableListOf<String>("retries ${item.retries} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDeclarationSnapshotRetriesRangeRule(
    var min: Int,
    var max: Int,
) : GlobalDeclarationSnapshotRule {
    override fun check(item: GlobalDeclarationSnapshot): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDeclarationSnapshotRequiredChecksumRule(var message: String) : GlobalDeclarationSnapshotRule {
    override fun check(item: GlobalDeclarationSnapshot): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalDeclarationSnapshotCodeLengthRule(var maxLength: Int) : GlobalDeclarationSnapshotRule {
    override fun check(item: GlobalDeclarationSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global declaration snapshot. */
data class GlobalDeclarationSnapshotValidator(var rules: MutableList<GlobalDeclarationSnapshotRule>) {
    fun validate(item: GlobalDeclarationSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalDeclarationSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalDeclarationSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalDeclarationSnapshotValidator(): GlobalDeclarationSnapshotValidator {
    return GlobalDeclarationSnapshotValidator(
        mutableListOf<GlobalDeclarationSnapshotRule>(
            GlobalDeclarationSnapshotLabelLengthRule(86400),
            GlobalDeclarationSnapshotArchivedGuardRule(120),
            GlobalDeclarationSnapshotRetriesRangeRule(16, 80),
            GlobalDeclarationSnapshotRequiredChecksumRule("global declaration snapshot needs an entry"),
            GlobalDeclarationSnapshotCodeLengthRule(64),
        ),
    )
}
