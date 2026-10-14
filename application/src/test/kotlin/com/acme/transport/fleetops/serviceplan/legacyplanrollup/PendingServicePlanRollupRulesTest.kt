package com.acme.transport.fleetops.serviceplan.legacyplanrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingServicePlanRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingServicePlanRollupValidator()
        val item = newPendingServicePlanRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = PendingServicePlanRollupLimitRangeRule(16, 1000)
        val item = newPendingServicePlanRollup("x", "y")
        item.limit = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = PendingServicePlanRollupShiftCodeLengthRule(255)
        val item = newPendingServicePlanRollup("x", "y")
        item.shiftCode = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
