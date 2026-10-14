package com.acme.transport.fleetops.defect.exception

import com.acme.transport.kit.text.normalize

/** A rule a global defect snapshot must pass. Check returns its problems, or none. */
interface GlobalDefectSnapshotRule {
    fun check(item: GlobalDefectSnapshot): MutableList<String>
}

data class GlobalDefectSnapshotRequiredDockRule(var message: String) : GlobalDefectSnapshotRule {
    override fun check(item: GlobalDefectSnapshot): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalDefectSnapshotAllowedNameRule(var allowed: MutableList<String>) : GlobalDefectSnapshotRule {
    override fun check(item: GlobalDefectSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is expired")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDefectSnapshotFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : GlobalDefectSnapshotRule {
    override fun check(item: GlobalDefectSnapshot): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class GlobalDefectSnapshotRequiredShiftCodeRule(var message: String) : GlobalDefectSnapshotRule {
    override fun check(item: GlobalDefectSnapshot): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalDefectSnapshotShiftCodeLengthRule(var maxLength: Int) : GlobalDefectSnapshotRule {
    override fun check(item: GlobalDefectSnapshot): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global defect snapshot. */
data class GlobalDefectSnapshotValidator(var rules: MutableList<GlobalDefectSnapshotRule>) {
    fun validate(item: GlobalDefectSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalDefectSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalDefectSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalDefectSnapshotValidator(): GlobalDefectSnapshotValidator {
    return GlobalDefectSnapshotValidator(
        mutableListOf<GlobalDefectSnapshotRule>(
            GlobalDefectSnapshotRequiredDockRule("global defect snapshot needs a value"),
            GlobalDefectSnapshotAllowedNameRule(mutableListOf<String>("rolling")),
            GlobalDefectSnapshotFloorValueRangeRule(0.1, 1856.7),
            GlobalDefectSnapshotRequiredShiftCodeRule("global defect snapshot needs an entry"),
            GlobalDefectSnapshotShiftCodeLengthRule(16),
        ),
    )
}
