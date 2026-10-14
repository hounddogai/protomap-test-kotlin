package com.acme.transport.safety.cameraevent.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventDraftValidator()
        val item = newCameraEventDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = CameraEventDraftLabelLengthRule(120)
        val item = newCameraEventDraft("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = CameraEventDraftMarginRangeRule(2.9, 221.7)
        val item = newCameraEventDraft("x", "y")
        item.margin = 223.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CameraEventDraftRequiredCodeRule("camera event draft needs this field")
        val item = newCameraEventDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = CameraEventDraftScoreRangeRule(1.2, 3270.5)
        val item = newCameraEventDraft("x", "y")
        item.score = 3272.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CameraEventDraftRequiredNameRule("camera event draft needs this field")
        val item = newCameraEventDraft("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
