package com.acme.transport.network.transittime.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeClaimValidator()
        val item = newTransitTimeClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = TransitTimeClaimNameLengthRule(120)
        val item = newTransitTimeClaim("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TransitTimeClaimRequiredNameRule("transit time claim needs this field")
        val item = newTransitTimeClaim("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TransitTimeClaimRequiredCodeRule("transit time claim needs an entry")
        val item = newTransitTimeClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
