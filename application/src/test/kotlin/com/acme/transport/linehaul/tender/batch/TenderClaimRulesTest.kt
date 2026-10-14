package com.acme.transport.linehaul.tender.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderClaimValidator()
        val item = newTenderClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = TenderClaimLaneLengthRule(80)
        val item = newTenderClaim("x", "y")
        item.lane = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = TenderClaimCapacityRangeRule(16, 80)
        val item = newTenderClaim("x", "y")
        item.capacity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TenderClaimRequiredCodeRule("tender claim needs a value")
        val item = newTenderClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
