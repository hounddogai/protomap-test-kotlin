package com.acme.transport.linehaul.laneplan.window

/** A rule a legacy lane plan bundle must pass. Check returns its problems, or none. */
interface LegacyLanePlanBundleRule {
    fun check(item: LegacyLanePlanBundle): MutableList<String>
}

data class LegacyLanePlanBundleChannelLengthRule(var maxLength: Int) : LegacyLanePlanBundleRule {
    override fun check(item: LegacyLanePlanBundle): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLanePlanBundleRequiredExternalRefRule(var message: String) : LegacyLanePlanBundleRule {
    override fun check(item: LegacyLanePlanBundle): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyLanePlanBundleTitleLengthRule(var maxLength: Int) : LegacyLanePlanBundleRule {
    override fun check(item: LegacyLanePlanBundle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLanePlanBundleRequiredChannelRule(var message: String) : LegacyLanePlanBundleRule {
    override fun check(item: LegacyLanePlanBundle): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyLanePlanBundleCodePrefixRule(var prefix: String) : LegacyLanePlanBundleRule {
    override fun check(item: LegacyLanePlanBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy lane plan bundle. */
data class LegacyLanePlanBundleValidator(var rules: MutableList<LegacyLanePlanBundleRule>) {
    fun validate(item: LegacyLanePlanBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyLanePlanBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyLanePlanBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyLanePlanBundleValidator(): LegacyLanePlanBundleValidator {
    return LegacyLanePlanBundleValidator(
        mutableListOf<LegacyLanePlanBundleRule>(
            LegacyLanePlanBundleChannelLengthRule(86400),
            LegacyLanePlanBundleRequiredExternalRefRule("legacy lane plan bundle needs this field"),
            LegacyLanePlanBundleTitleLengthRule(120),
            LegacyLanePlanBundleRequiredChannelRule("legacy lane plan bundle needs this field"),
            LegacyLanePlanBundleCodePrefixRule("OPS"),
        ),
    )
}
