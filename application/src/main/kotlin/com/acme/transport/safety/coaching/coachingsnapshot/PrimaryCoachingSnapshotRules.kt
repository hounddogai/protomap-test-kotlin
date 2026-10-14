package com.acme.transport.safety.coaching.coachingsnapshot

/** A rule a primary coaching snapshot must pass. Check returns its problems, or none. */
interface PrimaryCoachingSnapshotRule {
    fun check(item: PrimaryCoachingSnapshot): MutableList<String>
}

data class PrimaryCoachingSnapshotCodePrefixRule(var prefix: String) : PrimaryCoachingSnapshotRule {
    override fun check(item: PrimaryCoachingSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCoachingSnapshotTimezoneLengthRule(var maxLength: Int) : PrimaryCoachingSnapshotRule {
    override fun check(item: PrimaryCoachingSnapshot): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCoachingSnapshotCodeLengthRule(var maxLength: Int) : PrimaryCoachingSnapshotRule {
    override fun check(item: PrimaryCoachingSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCoachingSnapshotRequiredNameRule(var message: String) : PrimaryCoachingSnapshotRule {
    override fun check(item: PrimaryCoachingSnapshot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary coaching snapshot. */
data class PrimaryCoachingSnapshotValidator(var rules: MutableList<PrimaryCoachingSnapshotRule>) {
    fun validate(item: PrimaryCoachingSnapshot): MutableList<String> {
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

    fun requireValid(item: PrimaryCoachingSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCoachingSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCoachingSnapshotValidator(): PrimaryCoachingSnapshotValidator {
    return PrimaryCoachingSnapshotValidator(
        mutableListOf<PrimaryCoachingSnapshotRule>(
            PrimaryCoachingSnapshotCodePrefixRule("ACM"),
            PrimaryCoachingSnapshotTimezoneLengthRule(255),
            PrimaryCoachingSnapshotCodeLengthRule(86400),
            PrimaryCoachingSnapshotRequiredNameRule("primary coaching snapshot needs an entry"),
        ),
    )
}
