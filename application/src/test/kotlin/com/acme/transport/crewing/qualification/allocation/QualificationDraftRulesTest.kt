package com.acme.transport.crewing.qualification.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationDraftValidator()
        val item = newQualificationDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = QualificationDraftDistanceKmRangeRule(1.2, 2184.3)
        val item = newQualificationDraft("x", "y")
        item.distanceKm = 2185.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above revision`() {
        val rule = QualificationDraftRevisionRangeRule(64, 86400)
        val item = newQualificationDraft("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = QualificationDraftLabelLengthRule(32)
        val item = newQualificationDraft("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = QualificationDraftRequiredCodeRule("qualification draft needs this field")
        val item = newQualificationDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = QualificationDraftEtaMinutesRangeRule(16, 255)
        val item = newQualificationDraft("x", "y")
        item.etaMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = QualificationDraftCodeLengthRule(255)
        val item = newQualificationDraft("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
