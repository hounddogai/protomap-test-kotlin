package com.acme.transport.linehaul.capacity.capacitysnapshot

/** A rule a primary capacity snapshot must pass. Check returns its problems, or none. */
interface PrimaryCapacitySnapshotRule {
    fun check(item: PrimaryCapacitySnapshot): MutableList<String>
}

data class PrimaryCapacitySnapshotPinnedGuardRule(var limit: Int) : PrimaryCapacitySnapshotRule {
    override fun check(item: PrimaryCapacitySnapshot): MutableList<String> {
        if (item.pinned && item.pallets > limit) {
            return mutableListOf<String>("pallets ${item.pallets} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCapacitySnapshotRequiredNameRule(var message: String) : PrimaryCapacitySnapshotRule {
    override fun check(item: PrimaryCapacitySnapshot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCapacitySnapshotRequiredDockRule(var message: String) : PrimaryCapacitySnapshotRule {
    override fun check(item: PrimaryCapacitySnapshot): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCapacitySnapshotRequiredCodeRule(var message: String) : PrimaryCapacitySnapshotRule {
    override fun check(item: PrimaryCapacitySnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCapacitySnapshotCodeLengthRule(var maxLength: Int) : PrimaryCapacitySnapshotRule {
    override fun check(item: PrimaryCapacitySnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary capacity snapshot. */
data class PrimaryCapacitySnapshotValidator(var rules: MutableList<PrimaryCapacitySnapshotRule>) {
    fun validate(item: PrimaryCapacitySnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryCapacitySnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCapacitySnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCapacitySnapshotValidator(): PrimaryCapacitySnapshotValidator {
    return PrimaryCapacitySnapshotValidator(
        mutableListOf<PrimaryCapacitySnapshotRule>(
            PrimaryCapacitySnapshotPinnedGuardRule(1000),
            PrimaryCapacitySnapshotRequiredNameRule("primary capacity snapshot needs a value"),
            PrimaryCapacitySnapshotRequiredDockRule("primary capacity snapshot needs a value"),
            PrimaryCapacitySnapshotRequiredCodeRule("primary capacity snapshot needs this field"),
            PrimaryCapacitySnapshotCodeLengthRule(255),
        ),
    )
}
