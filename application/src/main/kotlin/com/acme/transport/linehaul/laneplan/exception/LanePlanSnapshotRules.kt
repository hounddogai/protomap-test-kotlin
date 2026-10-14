package com.acme.transport.linehaul.laneplan.exception

/** A rule a lane plan snapshot must pass. Check returns its problems, or none. */
interface LanePlanSnapshotRule {
    fun check(item: LanePlanSnapshot): MutableList<String>
}

data class LanePlanSnapshotEtaMinutesRangeRule(var min: Int, var max: Int) : LanePlanSnapshotRule {
    override fun check(item: LanePlanSnapshot): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanSnapshotCodePrefixRule(var prefix: String) : LanePlanSnapshotRule {
    override fun check(item: LanePlanSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanSnapshotRequiredShiftCodeRule(var message: String) : LanePlanSnapshotRule {
    override fun check(item: LanePlanSnapshot): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanSnapshotRequiredCodeRule(var message: String) : LanePlanSnapshotRule {
    override fun check(item: LanePlanSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanSnapshotRevisionRangeRule(var min: Int, var max: Int) : LanePlanSnapshotRule {
    override fun check(item: LanePlanSnapshot): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lane plan snapshot. */
data class LanePlanSnapshotValidator(var rules: MutableList<LanePlanSnapshotRule>) {
    fun validate(item: LanePlanSnapshot): MutableList<String> {
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

    fun requireValid(item: LanePlanSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanSnapshotValidator(): LanePlanSnapshotValidator {
    return LanePlanSnapshotValidator(
        mutableListOf<LanePlanSnapshotRule>(
            LanePlanSnapshotEtaMinutesRangeRule(32, 80),
            LanePlanSnapshotCodePrefixRule("REF"),
            LanePlanSnapshotRequiredShiftCodeRule("lane plan snapshot needs a value"),
            LanePlanSnapshotRequiredCodeRule("lane plan snapshot needs a value"),
            LanePlanSnapshotRevisionRangeRule(16, 86400),
        ),
    )
}
