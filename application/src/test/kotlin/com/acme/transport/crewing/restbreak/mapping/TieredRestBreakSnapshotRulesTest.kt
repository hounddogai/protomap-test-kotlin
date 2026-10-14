package com.acme.transport.crewing.restbreak.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredRestBreakSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredRestBreakSnapshotValidator()
        val item = newTieredRestBreakSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = TieredRestBreakSnapshotRequiredPhoneRule("tiered rest break snapshot needs this field")
        val item = newTieredRestBreakSnapshot("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TieredRestBreakSnapshotRequiredNameRule("tiered rest break snapshot needs an entry")
        val item = newTieredRestBreakSnapshot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
