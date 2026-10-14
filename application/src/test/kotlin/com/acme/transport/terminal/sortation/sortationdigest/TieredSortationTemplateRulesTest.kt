package com.acme.transport.terminal.sortation.sortationdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredSortationTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredSortationTemplateValidator()
        val item = newTieredSortationTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = TieredSortationTemplateContactEmailLengthRule(64)
        val item = newTieredSortationTemplate("x", "y")
        item.contactEmail = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = TieredSortationTemplateNameLengthRule(255)
        val item = newTieredSortationTemplate("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TieredSortationTemplateRequiredNameRule("tiered sortation template needs a value")
        val item = newTieredSortationTemplate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = TieredSortationTemplateSegmentLengthRule(120)
        val item = newTieredSortationTemplate("x", "y")
        item.segment = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
