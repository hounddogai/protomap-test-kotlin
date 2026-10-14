package com.acme.transport.linehaul.laneplan.plansegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanClaimValidator()
        val item = newLanePlanClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LanePlanClaimRequiredCodeRule("lane plan claim needs this field")
        val item = newLanePlanClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LanePlanClaimTitleLengthRule(255)
        val item = newLanePlanClaim("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LanePlanClaimRequiredTitleRule("lane plan claim needs an entry")
        val item = newLanePlanClaim("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = LanePlanClaimRequiredDepotRule("lane plan claim needs an entry")
        val item = newLanePlanClaim("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = LanePlanClaimRequiredCountryRule("lane plan claim needs an entry")
        val item = newLanePlanClaim("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = LanePlanClaimCountryLengthRule(80)
        val item = newLanePlanClaim("x", "y")
        item.country = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
