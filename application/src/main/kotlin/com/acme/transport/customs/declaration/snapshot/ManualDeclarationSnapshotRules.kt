package com.acme.transport.customs.declaration.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a manual declaration snapshot must pass. Check returns its problems, or none. */
interface ManualDeclarationSnapshotRule {
    fun check(item: ManualDeclarationSnapshot): MutableList<String>
}

data class ManualDeclarationSnapshotCategoryLengthRule(var maxLength: Int) : ManualDeclarationSnapshotRule {
    override fun check(item: ManualDeclarationSnapshot): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDeclarationSnapshotVehicleIdLengthRule(var maxLength: Int) : ManualDeclarationSnapshotRule {
    override fun check(item: ManualDeclarationSnapshot): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDeclarationSnapshotAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : ManualDeclarationSnapshotRule {
    override fun check(item: ManualDeclarationSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual declaration snapshot. */
data class ManualDeclarationSnapshotValidator(var rules: MutableList<ManualDeclarationSnapshotRule>) {
    fun validate(item: ManualDeclarationSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualDeclarationSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDeclarationSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDeclarationSnapshotValidator(): ManualDeclarationSnapshotValidator {
    return ManualDeclarationSnapshotValidator(
        mutableListOf<ManualDeclarationSnapshotRule>(
            ManualDeclarationSnapshotCategoryLengthRule(120),
            ManualDeclarationSnapshotVehicleIdLengthRule(80),
            ManualDeclarationSnapshotAllowedVehicleIdRule(mutableListOf<String>("primary")),
        ),
    )
}
