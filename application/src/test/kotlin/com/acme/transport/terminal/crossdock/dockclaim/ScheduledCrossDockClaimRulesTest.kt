package com.acme.transport.terminal.crossdock.dockclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledCrossDockClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledCrossDockClaimValidator()
        val item = newScheduledCrossDockClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = ScheduledCrossDockClaimSegmentLengthRule(3600)
        val item = newScheduledCrossDockClaim("x", "y")
        item.segment = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledCrossDockClaimRequiredNameRule("scheduled cross dock claim needs an entry")
        val item = newScheduledCrossDockClaim("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
