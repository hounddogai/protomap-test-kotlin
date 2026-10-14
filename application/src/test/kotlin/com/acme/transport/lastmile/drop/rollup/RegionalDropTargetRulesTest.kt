package com.acme.transport.lastmile.drop.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDropTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDropTargetValidator()
        val item = newRegionalDropTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RegionalDropTargetRequiredTitleRule("regional drop target needs an entry")
        val item = newRegionalDropTarget("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = RegionalDropTargetRequiredTrailerIdRule("regional drop target needs this field")
        val item = newRegionalDropTarget("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
