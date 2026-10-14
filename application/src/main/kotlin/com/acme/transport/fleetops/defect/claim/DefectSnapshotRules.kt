package com.acme.transport.fleetops.defect.claim

/** A rule a defect snapshot must pass. Check returns its problems, or none. */
interface DefectSnapshotRule {
    fun check(item: DefectSnapshot): MutableList<String>
}

data class DefectSnapshotNameLengthRule(var maxLength: Int) : DefectSnapshotRule {
    override fun check(item: DefectSnapshot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectSnapshotRequiredCodeRule(var message: String) : DefectSnapshotRule {
    override fun check(item: DefectSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectSnapshotCodePrefixRule(var prefix: String) : DefectSnapshotRule {
    override fun check(item: DefectSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect snapshot. */
data class DefectSnapshotValidator(var rules: MutableList<DefectSnapshotRule>) {
    fun validate(item: DefectSnapshot): MutableList<String> {
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

    fun requireValid(item: DefectSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectSnapshotValidator(): DefectSnapshotValidator {
    return DefectSnapshotValidator(
        mutableListOf<DefectSnapshotRule>(
            DefectSnapshotNameLengthRule(16),
            DefectSnapshotRequiredCodeRule("defect snapshot needs a value"),
            DefectSnapshotCodePrefixRule("ACM"),
        ),
    )
}
