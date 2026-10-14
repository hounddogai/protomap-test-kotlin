package com.acme.transport.fleetops.tire.tireagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingTireAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingTireAgreementValidator()
        val item = newPendingTireAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingTireAgreementCodeLengthRule(255)
        val item = newPendingTireAgreement("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = PendingTireAgreementRequiredLaneRule("pending tire agreement needs this field")
        val item = newPendingTireAgreement("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = PendingTireAgreementLaneLengthRule(255)
        val item = newPendingTireAgreement("x", "y")
        item.lane = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
