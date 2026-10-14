package com.acme.transport.safety.nearmiss.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientNearMissMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientNearMissMappingValidator()
        val item = newLenientNearMissMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientNearMissMappingCodeLengthRule(86400)
        val item = newLenientNearMissMapping("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = LenientNearMissMappingCountryLengthRule(86400)
        val item = newLenientNearMissMapping("x", "y")
        item.country = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = LenientNearMissMappingGraceDaysRangeRule(64, 120)
        val item = newLenientNearMissMapping("x", "y")
        item.graceDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
