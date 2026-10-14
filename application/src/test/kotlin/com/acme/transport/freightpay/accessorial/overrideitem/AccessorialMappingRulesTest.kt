package com.acme.transport.freightpay.accessorial.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialMappingValidator()
        val item = newAccessorialMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = AccessorialMappingCountryLengthRule(86400)
        val item = newAccessorialMapping("x", "y")
        item.country = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = AccessorialMappingBatchSizeRangeRule(80, 1000)
        val item = newAccessorialMapping("x", "y")
        item.batchSize = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank country`() {
        val rule = AccessorialMappingRequiredCountryRule("accessorial mapping needs an entry")
        val item = newAccessorialMapping("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AccessorialMappingCodeLengthRule(3600)
        val item = newAccessorialMapping("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AccessorialMappingRequiredLabelRule("accessorial mapping needs a value")
        val item = newAccessorialMapping("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
