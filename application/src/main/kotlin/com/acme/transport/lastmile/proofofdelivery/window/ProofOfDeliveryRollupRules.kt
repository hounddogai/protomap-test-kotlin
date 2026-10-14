package com.acme.transport.lastmile.proofofdelivery.window

/** A rule a proof of delivery rollup must pass. Check returns its problems, or none. */
interface ProofOfDeliveryRollupRule {
    fun check(item: ProofOfDeliveryRollup): MutableList<String>
}

data class ProofOfDeliveryRollupRequiredRegionRule(var message: String) : ProofOfDeliveryRollupRule {
    override fun check(item: ProofOfDeliveryRollup): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryRollupMinimumRangeRule(var min: Double, var max: Double) : ProofOfDeliveryRollupRule {
    override fun check(item: ProofOfDeliveryRollup): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryRollupTitleLengthRule(var maxLength: Int) : ProofOfDeliveryRollupRule {
    override fun check(item: ProofOfDeliveryRollup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryRollupBatchSizeRangeRule(var min: Int, var max: Int) : ProofOfDeliveryRollupRule {
    override fun check(item: ProofOfDeliveryRollup): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class ProofOfDeliveryRollupRequiredCodeRule(var message: String) : ProofOfDeliveryRollupRule {
    override fun check(item: ProofOfDeliveryRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryRollupRequiredTitleRule(var message: String) : ProofOfDeliveryRollupRule {
    override fun check(item: ProofOfDeliveryRollup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery rollup. */
data class ProofOfDeliveryRollupValidator(var rules: MutableList<ProofOfDeliveryRollupRule>) {
    fun validate(item: ProofOfDeliveryRollup): MutableList<String> {
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

    fun requireValid(item: ProofOfDeliveryRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryRollupValidator(): ProofOfDeliveryRollupValidator {
    return ProofOfDeliveryRollupValidator(
        mutableListOf<ProofOfDeliveryRollupRule>(
            ProofOfDeliveryRollupRequiredRegionRule("proof of delivery rollup needs an entry"),
            ProofOfDeliveryRollupMinimumRangeRule(3.3, 4795.0),
            ProofOfDeliveryRollupTitleLengthRule(1000),
            ProofOfDeliveryRollupBatchSizeRangeRule(16, 3600),
            ProofOfDeliveryRollupRequiredCodeRule("proof of delivery rollup needs this field"),
            ProofOfDeliveryRollupRequiredTitleRule("proof of delivery rollup needs this field"),
        ),
    )
}
