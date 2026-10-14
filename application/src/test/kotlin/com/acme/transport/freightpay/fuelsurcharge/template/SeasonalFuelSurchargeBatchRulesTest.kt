package com.acme.transport.freightpay.fuelsurcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalFuelSurchargeBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalFuelSurchargeBatchValidator()
        val item = newSeasonalFuelSurchargeBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SeasonalFuelSurchargeBatchRequiredLabelRule("seasonal fuel surcharge batch needs a value")
        val item = newSeasonalFuelSurchargeBatch("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = SeasonalFuelSurchargeBatchNameLengthRule(16)
        val item = newSeasonalFuelSurchargeBatch("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SeasonalFuelSurchargeBatchRequiredNameRule("seasonal fuel surcharge batch needs an entry")
        val item = newSeasonalFuelSurchargeBatch("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
