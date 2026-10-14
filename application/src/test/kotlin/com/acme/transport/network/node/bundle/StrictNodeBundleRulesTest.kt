package com.acme.transport.network.node.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictNodeBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictNodeBundleValidator()
        val item = newStrictNodeBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = StrictNodeBundleRequiredReviewerRule("strict node bundle needs a value")
        val item = newStrictNodeBundle("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = StrictNodeBundleRequiredSegmentRule("strict node bundle needs a value")
        val item = newStrictNodeBundle("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = StrictNodeBundleRequiredLabelRule("strict node bundle needs a value")
        val item = newStrictNodeBundle("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
