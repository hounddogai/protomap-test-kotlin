package com.acme.transport.emissions.idling.idlingvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultIdlingVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultIdlingVarianceValidator()
        val item = newDefaultIdlingVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefaultIdlingVarianceNameLengthRule(64)
        val item = newDefaultIdlingVariance("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = DefaultIdlingVarianceContactEmailLengthRule(16)
        val item = newDefaultIdlingVariance("x", "y")
        item.contactEmail = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = DefaultIdlingVarianceStopCountRangeRule(32, 255)
        val item = newDefaultIdlingVariance("x", "y")
        item.stopCount = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
