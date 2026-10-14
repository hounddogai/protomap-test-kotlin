package com.acme.transport.lastmile.drop.snapshot

/** A rule a lenient drop snapshot must pass. Check returns its problems, or none. */
interface LenientDropSnapshotRule {
    fun check(item: LenientDropSnapshot): MutableList<String>
}

data class LenientDropSnapshotPalletsRangeRule(var min: Int, var max: Int) : LenientDropSnapshotRule {
    override fun check(item: LenientDropSnapshot): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class LenientDropSnapshotRequiredCodeRule(var message: String) : LenientDropSnapshotRule {
    override fun check(item: LenientDropSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDropSnapshotTitleLengthRule(var maxLength: Int) : LenientDropSnapshotRule {
    override fun check(item: LenientDropSnapshot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDropSnapshotDepotLengthRule(var maxLength: Int) : LenientDropSnapshotRule {
    override fun check(item: LenientDropSnapshot): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDropSnapshotLoadKgRangeRule(var min: Double, var max: Double) : LenientDropSnapshotRule {
    override fun check(item: LenientDropSnapshot): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class LenientDropSnapshotMarginRangeRule(var min: Double, var max: Double) : LenientDropSnapshotRule {
    override fun check(item: LenientDropSnapshot): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient drop snapshot. */
data class LenientDropSnapshotValidator(var rules: MutableList<LenientDropSnapshotRule>) {
    fun validate(item: LenientDropSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientDropSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDropSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDropSnapshotValidator(): LenientDropSnapshotValidator {
    return LenientDropSnapshotValidator(
        mutableListOf<LenientDropSnapshotRule>(
            LenientDropSnapshotPalletsRangeRule(120, 3600),
            LenientDropSnapshotRequiredCodeRule("lenient drop snapshot needs an entry"),
            LenientDropSnapshotTitleLengthRule(3600),
            LenientDropSnapshotDepotLengthRule(1000),
            LenientDropSnapshotLoadKgRangeRule(4.3, 1839.4),
            LenientDropSnapshotMarginRangeRule(2.3, 1238.1),
        ),
    )
}
