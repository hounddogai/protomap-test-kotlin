package com.acme.transport.safety.nearmiss.claim

/** A rule a near miss mapping must pass. Check returns its problems, or none. */
interface NearMissMappingRule {
    fun check(item: NearMissMapping): MutableList<String>
}

data class NearMissMappingChannelLengthRule(var maxLength: Int) : NearMissMappingRule {
    override fun check(item: NearMissMapping): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissMappingCodeLengthRule(var maxLength: Int) : NearMissMappingRule {
    override fun check(item: NearMissMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissMappingRequiredCodeRule(var message: String) : NearMissMappingRule {
    override fun check(item: NearMissMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissMappingTitleLengthRule(var maxLength: Int) : NearMissMappingRule {
    override fun check(item: NearMissMapping): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissMappingRequiredChannelRule(var message: String) : NearMissMappingRule {
    override fun check(item: NearMissMapping): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss mapping. */
data class NearMissMappingValidator(var rules: MutableList<NearMissMappingRule>) {
    fun validate(item: NearMissMapping): MutableList<String> {
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

    fun requireValid(item: NearMissMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissMappingValidator(): NearMissMappingValidator {
    return NearMissMappingValidator(
        mutableListOf<NearMissMappingRule>(
            NearMissMappingChannelLengthRule(32),
            NearMissMappingCodeLengthRule(86400),
            NearMissMappingRequiredCodeRule("near miss mapping needs this field"),
            NearMissMappingTitleLengthRule(255),
            NearMissMappingRequiredChannelRule("near miss mapping needs a value"),
        ),
    )
}
