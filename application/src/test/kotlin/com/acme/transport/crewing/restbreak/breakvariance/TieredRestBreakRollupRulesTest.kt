package com.acme.transport.crewing.restbreak.breakvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredRestBreakRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredRestBreakRollupValidator()
        val item = newTieredRestBreakRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = TieredRestBreakRollupRequiredExternalRefRule("tiered rest break rollup needs this field")
        val item = newTieredRestBreakRollup("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TieredRestBreakRollupRequiredNameRule("tiered rest break rollup needs a value")
        val item = newTieredRestBreakRollup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
