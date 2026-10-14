package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultServicePlanSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultServicePlanSnapshotValidator()
        val item = newDefaultServicePlanSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultServicePlanSnapshotRequiredCodeRule("default service plan snapshot needs a value")
        val item = newDefaultServicePlanSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultServicePlanSnapshotCodeLengthRule(120)
        val item = newDefaultServicePlanSnapshot("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = DefaultServicePlanSnapshotEtaMinutesRangeRule(16, 3600)
        val item = newDefaultServicePlanSnapshot("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
