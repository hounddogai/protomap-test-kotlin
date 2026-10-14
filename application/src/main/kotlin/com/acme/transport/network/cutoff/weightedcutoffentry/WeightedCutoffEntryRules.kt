package com.acme.transport.network.cutoff.weightedcutoffentry

/** A rule a weighted cutoff entry must pass. Check returns its problems, or none. */
interface WeightedCutoffEntryRule {
    fun check(item: WeightedCutoffEntry): MutableList<String>
}

data class WeightedCutoffEntryOwnerIdLengthRule(var maxLength: Int) : WeightedCutoffEntryRule {
    override fun check(item: WeightedCutoffEntry): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCutoffEntryCodePrefixRule(var prefix: String) : WeightedCutoffEntryRule {
    override fun check(item: WeightedCutoffEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCutoffEntryEtaMinutesRangeRule(var min: Int, var max: Int) : WeightedCutoffEntryRule {
    override fun check(item: WeightedCutoffEntry): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCutoffEntryRequiredCodeRule(var message: String) : WeightedCutoffEntryRule {
    override fun check(item: WeightedCutoffEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedCutoffEntryLoadKgRangeRule(var min: Double, var max: Double) : WeightedCutoffEntryRule {
    override fun check(item: WeightedCutoffEntry): MutableList<String> {
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

data class WeightedCutoffEntryLabelLengthRule(var maxLength: Int) : WeightedCutoffEntryRule {
    override fun check(item: WeightedCutoffEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted cutoff entry. */
data class WeightedCutoffEntryValidator(var rules: MutableList<WeightedCutoffEntryRule>) {
    fun validate(item: WeightedCutoffEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedCutoffEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedCutoffEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedCutoffEntryValidator(): WeightedCutoffEntryValidator {
    return WeightedCutoffEntryValidator(
        mutableListOf<WeightedCutoffEntryRule>(
            WeightedCutoffEntryOwnerIdLengthRule(80),
            WeightedCutoffEntryCodePrefixRule("OPS"),
            WeightedCutoffEntryEtaMinutesRangeRule(120, 120),
            WeightedCutoffEntryRequiredCodeRule("weighted cutoff entry needs an entry"),
            WeightedCutoffEntryLoadKgRangeRule(1.7, 4192.9),
            WeightedCutoffEntryLabelLengthRule(80),
        ),
    )
}
