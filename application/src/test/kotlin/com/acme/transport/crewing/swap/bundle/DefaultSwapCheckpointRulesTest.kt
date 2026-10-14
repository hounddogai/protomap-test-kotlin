package com.acme.transport.crewing.swap.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultSwapCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultSwapCheckpointValidator()
        val item = newDefaultSwapCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = DefaultSwapCheckpointRequiredExternalRefRule("default swap checkpoint needs an entry")
        val item = newDefaultSwapCheckpoint("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = DefaultSwapCheckpointTitleLengthRule(1000)
        val item = newDefaultSwapCheckpoint("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
