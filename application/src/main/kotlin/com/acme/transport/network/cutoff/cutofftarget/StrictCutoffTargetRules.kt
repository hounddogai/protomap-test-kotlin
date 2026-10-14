package com.acme.transport.network.cutoff.cutofftarget

/** A rule a strict cutoff target must pass. Check returns its problems, or none. */
interface StrictCutoffTargetRule {
    fun check(item: StrictCutoffTarget): MutableList<String>
}

data class StrictCutoffTargetEtaMinutesRangeRule(var min: Int, var max: Int) : StrictCutoffTargetRule {
    override fun check(item: StrictCutoffTarget): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictCutoffTargetCodePrefixRule(var prefix: String) : StrictCutoffTargetRule {
    override fun check(item: StrictCutoffTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictCutoffTargetBatchSizeRangeRule(var min: Int, var max: Int) : StrictCutoffTargetRule {
    override fun check(item: StrictCutoffTarget): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictCutoffTargetRequiredCodeRule(var message: String) : StrictCutoffTargetRule {
    override fun check(item: StrictCutoffTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictCutoffTargetNameLengthRule(var maxLength: Int) : StrictCutoffTargetRule {
    override fun check(item: StrictCutoffTarget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict cutoff target. */
data class StrictCutoffTargetValidator(var rules: MutableList<StrictCutoffTargetRule>) {
    fun validate(item: StrictCutoffTarget): MutableList<String> {
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

    fun requireValid(item: StrictCutoffTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictCutoffTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictCutoffTargetValidator(): StrictCutoffTargetValidator {
    return StrictCutoffTargetValidator(
        mutableListOf<StrictCutoffTargetRule>(
            StrictCutoffTargetEtaMinutesRangeRule(32, 64),
            StrictCutoffTargetCodePrefixRule("OPS"),
            StrictCutoffTargetBatchSizeRangeRule(120, 86400),
            StrictCutoffTargetRequiredCodeRule("strict cutoff target needs this field"),
            StrictCutoffTargetNameLengthRule(255),
        ),
    )
}
