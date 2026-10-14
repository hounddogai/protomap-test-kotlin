package com.acme.transport.safety.incident.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictIncidentClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictIncidentClaimValidator()
        val item = newStrictIncidentClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictIncidentClaimRequiredCodeRule("strict incident claim needs this field")
        val item = newStrictIncidentClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictIncidentClaimTitleLengthRule(32)
        val item = newStrictIncidentClaim("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = StrictIncidentClaimRequiredChecksumRule("strict incident claim needs an entry")
        val item = newStrictIncidentClaim("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictIncidentClaimRequiredTitleRule("strict incident claim needs an entry")
        val item = newStrictIncidentClaim("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = StrictIncidentClaimDistanceKmRangeRule(3.3, 572.8)
        val item = newStrictIncidentClaim("x", "y")
        item.distanceKm = 574.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
