package com.acme.transport.fleetops.tire.quota

/** A rule a tire snapshot must pass. Check returns its problems, or none. */
interface TireSnapshotRule {
    fun check(item: TireSnapshot): MutableList<String>
}

data class TireSnapshotLabelLengthRule(var maxLength: Int) : TireSnapshotRule {
    override fun check(item: TireSnapshot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireSnapshotPhoneLengthRule(var maxLength: Int) : TireSnapshotRule {
    override fun check(item: TireSnapshot): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireSnapshotCodePrefixRule(var prefix: String) : TireSnapshotRule {
    override fun check(item: TireSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TireSnapshotRequiredLabelRule(var message: String) : TireSnapshotRule {
    override fun check(item: TireSnapshot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire snapshot. */
data class TireSnapshotValidator(var rules: MutableList<TireSnapshotRule>) {
    fun validate(item: TireSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultTireSnapshotValidator(): TireSnapshotValidator {
    return TireSnapshotValidator(
        mutableListOf<TireSnapshotRule>(
            TireSnapshotLabelLengthRule(32),
            TireSnapshotPhoneLengthRule(80),
            TireSnapshotCodePrefixRule("X"),
            TireSnapshotRequiredLabelRule("tire snapshot needs this field"),
        ),
    )
}
