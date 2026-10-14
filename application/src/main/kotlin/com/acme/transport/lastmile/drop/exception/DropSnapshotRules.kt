package com.acme.transport.lastmile.drop.exception

/** A rule a drop snapshot must pass. Check returns its problems, or none. */
interface DropSnapshotRule {
    fun check(item: DropSnapshot): MutableList<String>
}

data class DropSnapshotWeightRangeRule(var min: Double, var max: Double) : DropSnapshotRule {
    override fun check(item: DropSnapshot): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class DropSnapshotRequiredTitleRule(var message: String) : DropSnapshotRule {
    override fun check(item: DropSnapshot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropSnapshotSequenceRangeRule(var min: Int, var max: Int) : DropSnapshotRule {
    override fun check(item: DropSnapshot): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class DropSnapshotCodePrefixRule(var prefix: String) : DropSnapshotRule {
    override fun check(item: DropSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DropSnapshotRequiredUnitRule(var message: String) : DropSnapshotRule {
    override fun check(item: DropSnapshot): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop snapshot. */
data class DropSnapshotValidator(var rules: MutableList<DropSnapshotRule>) {
    fun validate(item: DropSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DropSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultDropSnapshotValidator(): DropSnapshotValidator {
    return DropSnapshotValidator(
        mutableListOf<DropSnapshotRule>(
            DropSnapshotWeightRangeRule(2.0, 985.8),
            DropSnapshotRequiredTitleRule("drop snapshot needs an entry"),
            DropSnapshotSequenceRangeRule(255, 3600),
            DropSnapshotCodePrefixRule("OPS"),
            DropSnapshotRequiredUnitRule("drop snapshot needs a value"),
        ),
    )
}
