package com.acme.transport.linehaul.tender.bundle

/** A rule a tender bundle must pass. Check returns its problems, or none. */
interface TenderBundleRule {
    fun check(item: TenderBundle): MutableList<String>
}

data class TenderBundleRequiredSourceRule(var message: String) : TenderBundleRule {
    override fun check(item: TenderBundle): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderBundleDriverIdLengthRule(var maxLength: Int) : TenderBundleRule {
    override fun check(item: TenderBundle): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderBundleRequiredTitleRule(var message: String) : TenderBundleRule {
    override fun check(item: TenderBundle): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderBundleGraceDaysRangeRule(var min: Int, var max: Int) : TenderBundleRule {
    override fun check(item: TenderBundle): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TenderBundleCodePrefixRule(var prefix: String) : TenderBundleRule {
    override fun check(item: TenderBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender bundle. */
data class TenderBundleValidator(var rules: MutableList<TenderBundleRule>) {
    fun validate(item: TenderBundle): MutableList<String> {
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

    fun requireValid(item: TenderBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderBundleValidator(): TenderBundleValidator {
    return TenderBundleValidator(
        mutableListOf<TenderBundleRule>(
            TenderBundleRequiredSourceRule("tender bundle needs an entry"),
            TenderBundleDriverIdLengthRule(86400),
            TenderBundleRequiredTitleRule("tender bundle needs an entry"),
            TenderBundleGraceDaysRangeRule(16, 255),
            TenderBundleCodePrefixRule("REF"),
        ),
    )
}
