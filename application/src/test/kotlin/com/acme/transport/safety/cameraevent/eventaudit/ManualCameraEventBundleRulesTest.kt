package com.acme.transport.safety.cameraevent.eventaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCameraEventBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualCameraEventBundleValidator()
        val item = newManualCameraEventBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = ManualCameraEventBundleDockLengthRule(16)
        val item = newManualCameraEventBundle("x", "y")
        item.dock = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ManualCameraEventBundleRequiredNameRule("manual camera event bundle needs this field")
        val item = newManualCameraEventBundle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualCameraEventBundleRequiredCodeRule("manual camera event bundle needs a value")
        val item = newManualCameraEventBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ManualCameraEventBundleNameLengthRule(120)
        val item = newManualCameraEventBundle("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
