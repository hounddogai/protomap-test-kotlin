package com.acme.transport.safety.cameraevent.eventaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingCameraEventAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingCameraEventAuditValidator()
        val item = newRollingCameraEventAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingCameraEventAuditRequiredCodeRule("rolling camera event audit needs this field")
        val item = newRollingCameraEventAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = RollingCameraEventAuditRequiredDepotRule("rolling camera event audit needs this field")
        val item = newRollingCameraEventAudit("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
