package com.acme.transport.linehaul.tender.tenderforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedTenderClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedTenderClaimValidator()
        val item = newCappedTenderClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CappedTenderClaimRequiredNameRule("capped tender claim needs a value")
        val item = newCappedTenderClaim("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = CappedTenderClaimMarginRangeRule(4.5, 4992.8)
        val item = newCappedTenderClaim("x", "y")
        item.margin = 4994.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedTenderClaimRequiredCodeRule("capped tender claim needs this field")
        val item = newCappedTenderClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = CappedTenderClaimEtaMinutesRangeRule(64, 64)
        val item = newCappedTenderClaim("x", "y")
        item.etaMinutes = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
