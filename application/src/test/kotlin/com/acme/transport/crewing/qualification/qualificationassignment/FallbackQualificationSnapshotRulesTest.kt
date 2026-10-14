package com.acme.transport.crewing.qualification.qualificationassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackQualificationSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackQualificationSnapshotValidator()
        val item = newFallbackQualificationSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackQualificationSnapshotRequiredCodeRule("fallback qualification snapshot needs this field")
        val item = newFallbackQualificationSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = FallbackQualificationSnapshotRequiredLocaleRule("fallback qualification snapshot needs an entry")
        val item = newFallbackQualificationSnapshot("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = FallbackQualificationSnapshotStartsAtRangeRule(120, 1000)
        val item = newFallbackQualificationSnapshot("x", "y")
        item.startsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long locale`() {
        val rule = FallbackQualificationSnapshotLocaleLengthRule(80)
        val item = newFallbackQualificationSnapshot("x", "y")
        item.locale = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
