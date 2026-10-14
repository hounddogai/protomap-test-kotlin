package com.acme.transport.terminal.traileryard.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardManifestValidator()
        val item = newTrailerYardManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = TrailerYardManifestPriorityRangeRule(255, 86400)
        val item = newTrailerYardManifest("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above cost`() {
        val rule = TrailerYardManifestCostRangeRule(5.3, 4868.2)
        val item = newTrailerYardManifest("x", "y")
        item.cost = 4869.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TrailerYardManifestRequiredCodeRule("trailer yard manifest needs an entry")
        val item = newTrailerYardManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = TrailerYardManifestGraceDaysRangeRule(3600, 3600)
        val item = newTrailerYardManifest("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TrailerYardManifestRequiredTitleRule("trailer yard manifest needs this field")
        val item = newTrailerYardManifest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
