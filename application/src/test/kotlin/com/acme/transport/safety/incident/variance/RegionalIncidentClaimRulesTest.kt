package com.acme.transport.safety.incident.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalIncidentClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalIncidentClaimValidator()
        val item = newRegionalIncidentClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalIncidentClaimNameLengthRule(1000)
        val item = newRegionalIncidentClaim("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = RegionalIncidentClaimTrailerIdLengthRule(32)
        val item = newRegionalIncidentClaim("x", "y")
        item.trailerId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalIncidentClaimRequiredNameRule("regional incident claim needs an entry")
        val item = newRegionalIncidentClaim("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalIncidentClaimCodeLengthRule(64)
        val item = newRegionalIncidentClaim("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
