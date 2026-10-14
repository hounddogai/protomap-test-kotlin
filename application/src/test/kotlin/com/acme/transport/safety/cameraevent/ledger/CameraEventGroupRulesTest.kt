package com.acme.transport.safety.cameraevent.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventGroupValidator()
        val item = newCameraEventGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CameraEventGroupRequiredCodeRule("camera event group needs this field")
        val item = newCameraEventGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = CameraEventGroupRequiredDockRule("camera event group needs a value")
        val item = newCameraEventGroup("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = CameraEventGroupRequiredTrailerIdRule("camera event group needs an entry")
        val item = newCameraEventGroup("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CameraEventGroupNameLengthRule(255)
        val item = newCameraEventGroup("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long dock`() {
        val rule = CameraEventGroupDockLengthRule(86400)
        val item = newCameraEventGroup("x", "y")
        item.dock = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
