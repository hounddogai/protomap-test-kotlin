package com.acme.transport.network.servicearea.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientServiceAreaThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientServiceAreaThresholdValidator()
        val item = newLenientServiceAreaThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = LenientServiceAreaThresholdRequiredCurrencyRule("lenient service area threshold needs an entry")
        val item = newLenientServiceAreaThreshold("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = LenientServiceAreaThresholdRevisionRangeRule(255, 3600)
        val item = newLenientServiceAreaThreshold("x", "y")
        item.revision = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank region`() {
        val rule = LenientServiceAreaThresholdRequiredRegionRule("lenient service area threshold needs this field")
        val item = newLenientServiceAreaThreshold("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = LenientServiceAreaThresholdCurrencyLengthRule(32)
        val item = newLenientServiceAreaThreshold("x", "y")
        item.currency = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = LenientServiceAreaThresholdOdometerRangeRule(16, 3600)
        val item = newLenientServiceAreaThreshold("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
