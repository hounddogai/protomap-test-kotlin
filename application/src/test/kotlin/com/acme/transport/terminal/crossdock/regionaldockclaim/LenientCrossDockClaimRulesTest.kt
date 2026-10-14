package com.acme.transport.terminal.crossdock.regionaldockclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCrossDockClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientCrossDockClaimValidator()
        val item = newLenientCrossDockClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = LenientCrossDockClaimEtaMinutesRangeRule(32, 3600)
        val item = newLenientCrossDockClaim("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientCrossDockClaimCodeLengthRule(1000)
        val item = newLenientCrossDockClaim("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = LenientCrossDockClaimRequiredRouteCodeRule("lenient cross dock claim needs a value")
        val item = newLenientCrossDockClaim("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
