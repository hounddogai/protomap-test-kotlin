package com.acme.transport.linehaul.capacity.budget

/** A rule a capacity manifest must pass. Check returns its problems, or none. */
interface CapacityManifestRule {
    fun check(item: CapacityManifest): MutableList<String>
}

data class CapacityManifestTitleLengthRule(var maxLength: Int) : CapacityManifestRule {
    override fun check(item: CapacityManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityManifestCodeLengthRule(var maxLength: Int) : CapacityManifestRule {
    override fun check(item: CapacityManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityManifestChannelLengthRule(var maxLength: Int) : CapacityManifestRule {
    override fun check(item: CapacityManifest): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity manifest. */
data class CapacityManifestValidator(var rules: MutableList<CapacityManifestRule>) {
    fun validate(item: CapacityManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacityManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityManifestValidator(): CapacityManifestValidator {
    return CapacityManifestValidator(
        mutableListOf<CapacityManifestRule>(
            CapacityManifestTitleLengthRule(1000),
            CapacityManifestCodeLengthRule(80),
            CapacityManifestChannelLengthRule(86400),
        ),
    )
}
