package com.acme.transport.customs.declaration.profile

/** A rule a pending declaration snapshot must pass. Check returns its problems, or none. */
interface PendingDeclarationSnapshotRule {
    fun check(item: PendingDeclarationSnapshot): MutableList<String>
}

data class PendingDeclarationSnapshotTitleLengthRule(var maxLength: Int) : PendingDeclarationSnapshotRule {
    override fun check(item: PendingDeclarationSnapshot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDeclarationSnapshotCostRangeRule(
    var min: Double,
    var max: Double,
) : PendingDeclarationSnapshotRule {
    override fun check(item: PendingDeclarationSnapshot): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingDeclarationSnapshotRequiredTimezoneRule(
    var message: String,
) : PendingDeclarationSnapshotRule {
    override fun check(item: PendingDeclarationSnapshot): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDeclarationSnapshotCodePrefixRule(var prefix: String) : PendingDeclarationSnapshotRule {
    override fun check(item: PendingDeclarationSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingDeclarationSnapshotTimezoneLengthRule(var maxLength: Int) : PendingDeclarationSnapshotRule {
    override fun check(item: PendingDeclarationSnapshot): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDeclarationSnapshotCodeLengthRule(var maxLength: Int) : PendingDeclarationSnapshotRule {
    override fun check(item: PendingDeclarationSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending declaration snapshot. */
data class PendingDeclarationSnapshotValidator(var rules: MutableList<PendingDeclarationSnapshotRule>) {
    fun validate(item: PendingDeclarationSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingDeclarationSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDeclarationSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDeclarationSnapshotValidator(): PendingDeclarationSnapshotValidator {
    return PendingDeclarationSnapshotValidator(
        mutableListOf<PendingDeclarationSnapshotRule>(
            PendingDeclarationSnapshotTitleLengthRule(16),
            PendingDeclarationSnapshotCostRangeRule(8.2, 4978.5),
            PendingDeclarationSnapshotRequiredTimezoneRule("pending declaration snapshot needs an entry"),
            PendingDeclarationSnapshotCodePrefixRule("ACM"),
            PendingDeclarationSnapshotTimezoneLengthRule(32),
            PendingDeclarationSnapshotCodeLengthRule(255),
        ),
    )
}
