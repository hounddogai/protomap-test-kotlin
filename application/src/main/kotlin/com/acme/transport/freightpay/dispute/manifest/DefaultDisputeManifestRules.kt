package com.acme.transport.freightpay.dispute.manifest

/** A rule a default dispute manifest must pass. Check returns its problems, or none. */
interface DefaultDisputeManifestRule {
    fun check(item: DefaultDisputeManifest): MutableList<String>
}

data class DefaultDisputeManifestBatchSizeRangeRule(var min: Int, var max: Int) : DefaultDisputeManifestRule {
    override fun check(item: DefaultDisputeManifest): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDisputeManifestRequiredTitleRule(var message: String) : DefaultDisputeManifestRule {
    override fun check(item: DefaultDisputeManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultDisputeManifestPalletsRangeRule(var min: Int, var max: Int) : DefaultDisputeManifestRule {
    override fun check(item: DefaultDisputeManifest): MutableList<String> {
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

data class DefaultDisputeManifestUpdatedAtRangeRule(var min: Int, var max: Int) : DefaultDisputeManifestRule {
    override fun check(item: DefaultDisputeManifest): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDisputeManifestCodeLengthRule(var maxLength: Int) : DefaultDisputeManifestRule {
    override fun check(item: DefaultDisputeManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDisputeManifestLabelLengthRule(var maxLength: Int) : DefaultDisputeManifestRule {
    override fun check(item: DefaultDisputeManifest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default dispute manifest. */
data class DefaultDisputeManifestValidator(var rules: MutableList<DefaultDisputeManifestRule>) {
    fun validate(item: DefaultDisputeManifest): MutableList<String> {
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

    fun requireValid(item: DefaultDisputeManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultDisputeManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultDisputeManifestValidator(): DefaultDisputeManifestValidator {
    return DefaultDisputeManifestValidator(
        mutableListOf<DefaultDisputeManifestRule>(
            DefaultDisputeManifestBatchSizeRangeRule(80, 80),
            DefaultDisputeManifestRequiredTitleRule("default dispute manifest needs a value"),
            DefaultDisputeManifestPalletsRangeRule(255, 3600),
            DefaultDisputeManifestUpdatedAtRangeRule(80, 86400),
            DefaultDisputeManifestCodeLengthRule(3600),
            DefaultDisputeManifestLabelLengthRule(120),
        ),
    )
}
