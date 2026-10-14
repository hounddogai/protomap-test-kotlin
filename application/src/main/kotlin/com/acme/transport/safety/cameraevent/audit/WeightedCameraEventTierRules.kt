package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.kit.text.normalize

/** A rule a weighted camera event tier must pass. Check returns its problems, or none. */
interface WeightedCameraEventTierRule {
    fun check(item: WeightedCameraEventTier): MutableList<String>
}

data class WeightedCameraEventTierRequiredCarrierRule(var message: String) : WeightedCameraEventTierRule {
    override fun check(item: WeightedCameraEventTier): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedCameraEventTierAllowedNameRule(
    var allowed: MutableList<String>,
) : WeightedCameraEventTierRule {
    override fun check(item: WeightedCameraEventTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCameraEventTierNameLengthRule(var maxLength: Int) : WeightedCameraEventTierRule {
    override fun check(item: WeightedCameraEventTier): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCameraEventTierCodeLengthRule(var maxLength: Int) : WeightedCameraEventTierRule {
    override fun check(item: WeightedCameraEventTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCameraEventTierCarrierLengthRule(var maxLength: Int) : WeightedCameraEventTierRule {
    override fun check(item: WeightedCameraEventTier): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCameraEventTierRequiredNameRule(var message: String) : WeightedCameraEventTierRule {
    override fun check(item: WeightedCameraEventTier): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted camera event tier. */
data class WeightedCameraEventTierValidator(var rules: MutableList<WeightedCameraEventTierRule>) {
    fun validate(item: WeightedCameraEventTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedCameraEventTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedCameraEventTier>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedCameraEventTierValidator(): WeightedCameraEventTierValidator {
    return WeightedCameraEventTierValidator(
        mutableListOf<WeightedCameraEventTierRule>(
            WeightedCameraEventTierRequiredCarrierRule("weighted camera event tier needs this field"),
            WeightedCameraEventTierAllowedNameRule(mutableListOf<String>()),
            WeightedCameraEventTierNameLengthRule(16),
            WeightedCameraEventTierCodeLengthRule(120),
            WeightedCameraEventTierCarrierLengthRule(16),
            WeightedCameraEventTierRequiredNameRule("weighted camera event tier needs an entry"),
        ),
    )
}
