package com.acme.transport.terminal.crossdock.snapshot

/** A rule a default cross dock snapshot must pass. Check returns its problems, or none. */
interface DefaultCrossDockSnapshotRule {
    fun check(item: DefaultCrossDockSnapshot): MutableList<String>
}

data class DefaultCrossDockSnapshotCodePrefixRule(var prefix: String) : DefaultCrossDockSnapshotRule {
    override fun check(item: DefaultCrossDockSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCrossDockSnapshotRequiredLaneRule(var message: String) : DefaultCrossDockSnapshotRule {
    override fun check(item: DefaultCrossDockSnapshot): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCrossDockSnapshotRequiredShiftCodeRule(var message: String) : DefaultCrossDockSnapshotRule {
    override fun check(item: DefaultCrossDockSnapshot): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCrossDockSnapshotShiftCodeLengthRule(var maxLength: Int) : DefaultCrossDockSnapshotRule {
    override fun check(item: DefaultCrossDockSnapshot): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default cross dock snapshot. */
data class DefaultCrossDockSnapshotValidator(var rules: MutableList<DefaultCrossDockSnapshotRule>) {
    fun validate(item: DefaultCrossDockSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultCrossDockSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCrossDockSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCrossDockSnapshotValidator(): DefaultCrossDockSnapshotValidator {
    return DefaultCrossDockSnapshotValidator(
        mutableListOf<DefaultCrossDockSnapshotRule>(
            DefaultCrossDockSnapshotCodePrefixRule("X"),
            DefaultCrossDockSnapshotRequiredLaneRule("default cross dock snapshot needs a value"),
            DefaultCrossDockSnapshotRequiredShiftCodeRule("default cross dock snapshot needs this field"),
            DefaultCrossDockSnapshotShiftCodeLengthRule(80),
        ),
    )
}
