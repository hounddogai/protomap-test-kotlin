package com.acme.transport.network.cutoff.preference

/** A rule a cutoff window must pass. Check returns its problems, or none. */
interface CutoffWindowRule {
    fun check(item: CutoffWindow): MutableList<String>
}

data class CutoffWindowCostRangeRule(var min: Double, var max: Double) : CutoffWindowRule {
    override fun check(item: CutoffWindow): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CutoffWindowRequiredLabelRule(var message: String) : CutoffWindowRule {
    override fun check(item: CutoffWindow): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffWindowCodeLengthRule(var maxLength: Int) : CutoffWindowRule {
    override fun check(item: CutoffWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffWindowRequiredLaneRule(var message: String) : CutoffWindowRule {
    override fun check(item: CutoffWindow): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffWindowRequiredDepotRule(var message: String) : CutoffWindowRule {
    override fun check(item: CutoffWindow): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff window. */
data class CutoffWindowValidator(var rules: MutableList<CutoffWindowRule>) {
    fun validate(item: CutoffWindow): MutableList<String> {
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

    fun requireValid(item: CutoffWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffWindowValidator(): CutoffWindowValidator {
    return CutoffWindowValidator(
        mutableListOf<CutoffWindowRule>(
            CutoffWindowCostRangeRule(6.3, 1977.7),
            CutoffWindowRequiredLabelRule("cutoff window needs an entry"),
            CutoffWindowCodeLengthRule(86400),
            CutoffWindowRequiredLaneRule("cutoff window needs an entry"),
            CutoffWindowRequiredDepotRule("cutoff window needs an entry"),
        ),
    )
}
