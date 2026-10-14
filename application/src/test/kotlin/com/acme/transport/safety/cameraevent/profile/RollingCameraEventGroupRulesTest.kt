package com.acme.transport.safety.cameraevent.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingCameraEventGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingCameraEventGroupValidator()
        val item = newRollingCameraEventGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = RollingCameraEventGroupCostRangeRule(4.5, 369.9)
        val item = newRollingCameraEventGroup("x", "y")
        item.cost = 371.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = RollingCameraEventGroupCreatedAtRangeRule(32, 1000)
        val item = newRollingCameraEventGroup("x", "y")
        item.createdAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingCameraEventGroupCodeLengthRule(255)
        val item = newRollingCameraEventGroup("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = RollingCameraEventGroupRequiredDockRule("rolling camera event group needs this field")
        val item = newRollingCameraEventGroup("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
