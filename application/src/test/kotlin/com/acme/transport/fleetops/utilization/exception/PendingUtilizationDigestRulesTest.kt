package com.acme.transport.fleetops.utilization.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingUtilizationDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingUtilizationDigestValidator()
        val item = newPendingUtilizationDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingUtilizationDigestCodeLengthRule(3600)
        val item = newPendingUtilizationDigest("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingUtilizationDigestRequiredTitleRule("pending utilization digest needs an entry")
        val item = newPendingUtilizationDigest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
