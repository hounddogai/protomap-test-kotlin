package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.kit.text.normalize

/** A rule a weighted fuel surcharge notice must pass. Check returns its problems, or none. */
interface WeightedFuelSurchargeNoticeRule {
    fun check(item: WeightedFuelSurchargeNotice): MutableList<String>
}

data class WeightedFuelSurchargeNoticeRequiredChecksumRule(
    var message: String,
) : WeightedFuelSurchargeNoticeRule {
    override fun check(item: WeightedFuelSurchargeNotice): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelSurchargeNoticeVersionRangeRule(
    var min: Int,
    var max: Int,
) : WeightedFuelSurchargeNoticeRule {
    override fun check(item: WeightedFuelSurchargeNotice): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class WeightedFuelSurchargeNoticeAllowedChecksumRule(
    var allowed: MutableList<String>,
) : WeightedFuelSurchargeNoticeRule {
    override fun check(item: WeightedFuelSurchargeNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelSurchargeNoticeCodePrefixRule(var prefix: String) : WeightedFuelSurchargeNoticeRule {
    override fun check(item: WeightedFuelSurchargeNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFuelSurchargeNoticeRequiredCodeRule(var message: String) : WeightedFuelSurchargeNoticeRule {
    override fun check(item: WeightedFuelSurchargeNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted fuel surcharge notice. */
data class WeightedFuelSurchargeNoticeValidator(var rules: MutableList<WeightedFuelSurchargeNoticeRule>) {
    fun validate(item: WeightedFuelSurchargeNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedFuelSurchargeNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedFuelSurchargeNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedFuelSurchargeNoticeValidator(): WeightedFuelSurchargeNoticeValidator {
    return WeightedFuelSurchargeNoticeValidator(
        mutableListOf<WeightedFuelSurchargeNoticeRule>(
            WeightedFuelSurchargeNoticeRequiredChecksumRule("weighted fuel surcharge notice needs a value"),
            WeightedFuelSurchargeNoticeVersionRangeRule(120, 1000),
            WeightedFuelSurchargeNoticeAllowedChecksumRule(mutableListOf<String>("legacy", "strict")),
            WeightedFuelSurchargeNoticeCodePrefixRule("ACM"),
            WeightedFuelSurchargeNoticeRequiredCodeRule("weighted fuel surcharge notice needs this field"),
        ),
    )
}
