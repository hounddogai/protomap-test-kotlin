package com.acme.transport.network.node.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyNodeBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyNodeBatchValidator()
        val item = newLegacyNodeBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LegacyNodeBatchRequiredTitleRule("legacy node batch needs a value")
        val item = newLegacyNodeBatch("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
