package com.acme.transport.terminal.crossdock.profile

/** A rule a capped cross dock snapshot must pass. Check returns its problems, or none. */
interface CappedCrossDockSnapshotRule {
    fun check(item: CappedCrossDockSnapshot): MutableList<String>
}

data class CappedCrossDockSnapshotRequiredPhoneRule(var message: String) : CappedCrossDockSnapshotRule {
    override fun check(item: CappedCrossDockSnapshot): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCrossDockSnapshotRequiredNameRule(var message: String) : CappedCrossDockSnapshotRule {
    override fun check(item: CappedCrossDockSnapshot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCrossDockSnapshotCodeLengthRule(var maxLength: Int) : CappedCrossDockSnapshotRule {
    override fun check(item: CappedCrossDockSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCrossDockSnapshotEndsAtRangeRule(var min: Int, var max: Int) : CappedCrossDockSnapshotRule {
    override fun check(item: CappedCrossDockSnapshot): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped cross dock snapshot. */
data class CappedCrossDockSnapshotValidator(var rules: MutableList<CappedCrossDockSnapshotRule>) {
    fun validate(item: CappedCrossDockSnapshot): MutableList<String> {
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

    fun requireValid(item: CappedCrossDockSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCrossDockSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCrossDockSnapshotValidator(): CappedCrossDockSnapshotValidator {
    return CappedCrossDockSnapshotValidator(
        mutableListOf<CappedCrossDockSnapshotRule>(
            CappedCrossDockSnapshotRequiredPhoneRule("capped cross dock snapshot needs this field"),
            CappedCrossDockSnapshotRequiredNameRule("capped cross dock snapshot needs an entry"),
            CappedCrossDockSnapshotCodeLengthRule(120),
            CappedCrossDockSnapshotEndsAtRangeRule(80, 3600),
        ),
    )
}
