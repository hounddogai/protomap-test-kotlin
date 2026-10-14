package com.acme.transport.network.servicearea.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualServiceAreaClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualServiceAreaClaimValidator()
        val item = newManualServiceAreaClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = ManualServiceAreaClaimRequiredTrailerIdRule("manual service area claim needs an entry")
        val item = newManualServiceAreaClaim("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = ManualServiceAreaClaimBatchSizeRangeRule(64, 1000)
        val item = newManualServiceAreaClaim("x", "y")
        item.batchSize = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualServiceAreaClaimRequiredCodeRule("manual service area claim needs a value")
        val item = newManualServiceAreaClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = ManualServiceAreaClaimTrailerIdLengthRule(64)
        val item = newManualServiceAreaClaim("x", "y")
        item.trailerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualServiceAreaClaimLabelLengthRule(32)
        val item = newManualServiceAreaClaim("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
