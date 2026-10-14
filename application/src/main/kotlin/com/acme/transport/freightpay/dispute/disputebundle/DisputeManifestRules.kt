package com.acme.transport.freightpay.dispute.disputebundle

/** A rule a dispute manifest must pass. Check returns its problems, or none. */
interface DisputeManifestRule {
    fun check(item: DisputeManifest): MutableList<String>
}

data class DisputeManifestRequiredCodeRule(var message: String) : DisputeManifestRule {
    override fun check(item: DisputeManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeManifestQuantityRangeRule(var min: Int, var max: Int) : DisputeManifestRule {
    override fun check(item: DisputeManifest): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DisputeManifestCodeLengthRule(var maxLength: Int) : DisputeManifestRule {
    override fun check(item: DisputeManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeManifestTitleLengthRule(var maxLength: Int) : DisputeManifestRule {
    override fun check(item: DisputeManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute manifest. */
data class DisputeManifestValidator(var rules: MutableList<DisputeManifestRule>) {
    fun validate(item: DisputeManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DisputeManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeManifestValidator(): DisputeManifestValidator {
    return DisputeManifestValidator(
        mutableListOf<DisputeManifestRule>(
            DisputeManifestRequiredCodeRule("dispute manifest needs a value"),
            DisputeManifestQuantityRangeRule(32, 3600),
            DisputeManifestCodeLengthRule(120),
            DisputeManifestTitleLengthRule(16),
        ),
    )
}
