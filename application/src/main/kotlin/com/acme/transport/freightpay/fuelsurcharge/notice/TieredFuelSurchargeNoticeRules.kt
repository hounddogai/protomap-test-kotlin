package com.acme.transport.freightpay.fuelsurcharge.notice

/** A rule a tiered fuel surcharge notice must pass. Check returns its problems, or none. */
interface TieredFuelSurchargeNoticeRule {
    fun check(item: TieredFuelSurchargeNotice): MutableList<String>
}

data class TieredFuelSurchargeNoticeLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : TieredFuelSurchargeNoticeRule {
    override fun check(item: TieredFuelSurchargeNotice): MutableList<String> {
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

data class TieredFuelSurchargeNoticeQuantityRangeRule(
    var min: Int,
    var max: Int,
) : TieredFuelSurchargeNoticeRule {
    override fun check(item: TieredFuelSurchargeNotice): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelSurchargeNoticeBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : TieredFuelSurchargeNoticeRule {
    override fun check(item: TieredFuelSurchargeNotice): MutableList<String> {
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

data class TieredFuelSurchargeNoticeRequiredLabelRule(var message: String) : TieredFuelSurchargeNoticeRule {
    override fun check(item: TieredFuelSurchargeNotice): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelSurchargeNoticeLabelLengthRule(var maxLength: Int) : TieredFuelSurchargeNoticeRule {
    override fun check(item: TieredFuelSurchargeNotice): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered fuel surcharge notice. */
data class TieredFuelSurchargeNoticeValidator(var rules: MutableList<TieredFuelSurchargeNoticeRule>) {
    fun validate(item: TieredFuelSurchargeNotice): MutableList<String> {
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

    fun requireValid(item: TieredFuelSurchargeNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredFuelSurchargeNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredFuelSurchargeNoticeValidator(): TieredFuelSurchargeNoticeValidator {
    return TieredFuelSurchargeNoticeValidator(
        mutableListOf<TieredFuelSurchargeNoticeRule>(
            TieredFuelSurchargeNoticeLoadKgRangeRule(7.3, 866.0),
            TieredFuelSurchargeNoticeQuantityRangeRule(32, 120),
            TieredFuelSurchargeNoticeBatchSizeRangeRule(80, 1000),
            TieredFuelSurchargeNoticeRequiredLabelRule("tiered fuel surcharge notice needs an entry"),
            TieredFuelSurchargeNoticeLabelLengthRule(255),
        ),
    )
}
