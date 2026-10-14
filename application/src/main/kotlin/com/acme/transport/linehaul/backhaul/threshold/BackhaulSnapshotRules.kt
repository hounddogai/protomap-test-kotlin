package com.acme.transport.linehaul.backhaul.threshold

/** A rule a backhaul snapshot must pass. Check returns its problems, or none. */
interface BackhaulSnapshotRule {
    fun check(item: BackhaulSnapshot): MutableList<String>
}

data class BackhaulSnapshotNameLengthRule(var maxLength: Int) : BackhaulSnapshotRule {
    override fun check(item: BackhaulSnapshot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulSnapshotRequiredNameRule(var message: String) : BackhaulSnapshotRule {
    override fun check(item: BackhaulSnapshot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulSnapshotTitleLengthRule(var maxLength: Int) : BackhaulSnapshotRule {
    override fun check(item: BackhaulSnapshot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul snapshot. */
data class BackhaulSnapshotValidator(var rules: MutableList<BackhaulSnapshotRule>) {
    fun validate(item: BackhaulSnapshot): MutableList<String> {
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

    fun requireValid(item: BackhaulSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulSnapshotValidator(): BackhaulSnapshotValidator {
    return BackhaulSnapshotValidator(
        mutableListOf<BackhaulSnapshotRule>(
            BackhaulSnapshotNameLengthRule(1000),
            BackhaulSnapshotRequiredNameRule("backhaul snapshot needs an entry"),
            BackhaulSnapshotTitleLengthRule(3600),
        ),
    )
}
